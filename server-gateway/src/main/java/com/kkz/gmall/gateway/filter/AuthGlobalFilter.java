package com.kkz.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.result.ResultCodeEnum;
import com.kkz.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Autowired
    private RedisTemplate redisTemplate;
    //读取白名单
    @Value("${authUrls.url}")
    private String authUrls;
    // 路径匹配
    private AntPathMatcher matcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        // 获取资源路径
        String path = request.getURI().getPath();
        // 内部接口提示没有权限
        if (matcher.match("/api/**/inner/**", path)) {
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 获取用户id
        String userId = this.getUserId(request);
        //判断ip是否被盗
        if ("-1".equals(userId)) {
            return out(response, ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //判断
        if (matcher.match("/api/**/auth/**", path)) {
            //判断未登录
            if (StringUtils.isEmpty(userId)) {
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //认证白名单
        if (!StringUtils.isEmpty(authUrls)) {
            String[] split = authUrls.split(",");
            for (String url : split) {
                if (path.indexOf(url) != -1 && StringUtils.isEmpty(userId)) {
                    //表示包含白名单的路径，并且没有登录
                    //设置状态码
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    //设置地址 location
                    //originUrl 当前请求的访问地址
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                    //重定向
                    return response.setComplete();
                }
            }
        }
        //获取临时id
        String userTempId = this.getUserTempId(request);
        //存储userId到请求
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if (!StringUtils.isEmpty(userId)) {
                //存储到request
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)) {
                //存储到request
                request.mutate().header("userTempId", userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }
    /**
     * 获取用户临时id
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        //从头信息
        List<String> list = request.getHeaders().get("userTempId");
        //判断
        if (!CollectionUtils.isEmpty(list)) {
            userTempId = list.get(0);
        }
        //判断，从cookie取
        if (StringUtils.isEmpty(userTempId)) {
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if (cookie != null) {
                userTempId = cookie.getValue();
            }
        }
        return userTempId;
    }
    /**
     * 获取用户id
     * 分析：有几种请求
     * 1.获取不到 ""
     * 2.获取到  userId
     * 3.ip不对应 盗取ip  -1
     *
     *
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        //定义变量记录token
        String token = null;
        //获取token --头信息
        List<String> list = request.getHeaders().get("token");
        //判断
        if (!CollectionUtils.isEmpty(list)) {
            token = list.get(0);
        }
        //获取token --cookie
        if (StringUtils.isEmpty(token)) {
            //获取所有的cookie
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie cookie = cookies.getFirst("token");
            //判断
            if (cookie != null) {
                token = cookie.getValue();
            }
        }
        //获取数据
        //判断
        if (!StringUtils.isEmpty(token)) {
            //从redis中获取
            String strJson = (String) redisTemplate.opsForValue().get("user:login:" + token);
            if (strJson == null) {
                // 没有从redis中获取到对应的信息，登录超时重新登录
                return "";
            }
            //转换
            JSONObject jsonObject = JSONObject.parseObject(strJson);
            //判断ip
            String ip = jsonObject.getString("ip");
            //获取当前请求的ip
            String curIp = IpUtil.getGatwayIpAddress(request);
            //判断ip
            if (curIp.equals(ip)) {
                return jsonObject.getString("userId");
            } else {
                return "-1";
            }
        }
        //没有用户id
        return "";
    }
    /**
     * 设置响应结果
     * @param response
     * @param permission
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        Result<Object> build = Result.build(null, permission);
        byte[] bytes = JSONObject.toJSONString(build).getBytes(StandardCharsets.UTF_8);
        //获取DataBuffer
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        //中文乱码处理
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }
}
