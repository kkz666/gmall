package com.kkz.gmall.user.client.impl;

import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class UserDegradeFeignClient implements UserFeignClient {
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        return null;
    }
}
