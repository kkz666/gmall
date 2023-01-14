package com.kkz.gmall.user.service;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.user.UserInfo;

import javax.servlet.http.HttpServletRequest;

public interface UserService {
    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    Result login(UserInfo userInfo, HttpServletRequest request);
    Result logout(HttpServletRequest request);
}
