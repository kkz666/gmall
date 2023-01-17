package com.kkz.gmall.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.util.IpUtil;
import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.model.user.UserInfo;
import com.kkz.gmall.user.mapper.UserAddressMapper;
import com.kkz.gmall.user.mapper.UserInfoMapper;
import com.kkz.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserAddressMapper userAddressMapper;
    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    @Override
    public Result login(UserInfo userInfo, HttpServletRequest request) {
        //select *from userinfo where login_name=? and passwd=?
        //处理密码加密
        String newPass = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        //封装条件
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", userInfo.getLoginName());
        queryWrapper.eq("passwd", newPass);
        UserInfo user = userInfoMapper.selectOne(queryWrapper);
        if(user != null){
            //生成token
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            //获取当前登录用户ip
            String ip = IpUtil.getIpAddress(request);
            JSONObject object = new JSONObject();
            object.put("ip", ip);
            object.put("userId", user.getId());
            //存储到redis   key: token value: 用户信息
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,
                    object.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            //返回页面信息
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("nickName", user.getNickName());
            resultMap.put("token", token);
            return Result.ok(resultMap);
        }else{
            return Result.fail().message("用户名或者密码错误！！！");
        }
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @Override
    public Result logout(HttpServletRequest request) {
        //请求redis中的数据
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + request.getHeader("token"));
        return Result.ok();
    }
    /**
     * 查询用户地址列表
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        //select*from user_address where user_id=?
        //封装查询条件
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userAddressMapper.selectList(queryWrapper);
    }

}
