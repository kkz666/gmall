package com.kkz.gmall.user.client;

import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.user.client.impl.UserDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
@FeignClient(value = "service-user",fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {
    /**
     * /api/user/inner/findUserAddressListByUserId/{userId}
     * 查询用户地址列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId);
}
