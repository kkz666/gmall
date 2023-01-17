package com.kkz.gmall.user.service;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.model.user.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {
    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    Result login(UserInfo userInfo, HttpServletRequest request);
    Result logout(HttpServletRequest request);
    /**
     * 查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> findUserAddressListByUserId(Long userId);
}
