package com.kkz.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.util.IpUtil;
import com.kkz.gmall.model.user.UserInfo;
import com.kkz.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class PassportController {
    @Autowired
    private UserService userService;

    /**
     *   ///api/user/passport/login
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){
        return userService.login(userInfo, request);
    }
    /**
     * /api/user/passport/logout
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        return userService.logout(request);
    }
}
