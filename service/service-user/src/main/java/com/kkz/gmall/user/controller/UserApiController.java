package com.kkz.gmall.user.controller;

import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/inner")
public class UserApiController {
    @Autowired
    UserService userService;
    /**
     * /api/user/inner/findUserAddressListByUserId/{userId}
     * 查询用户地址列表
     * @param userId
     * @return
     */
    @GetMapping("/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId ){
        List<UserAddress> userAddressList = this.userService.findUserAddressListByUserId(userId);
        return userAddressList;
    }
}
