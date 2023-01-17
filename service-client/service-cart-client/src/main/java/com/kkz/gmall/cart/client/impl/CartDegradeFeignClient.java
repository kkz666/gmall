package com.kkz.gmall.cart.client.impl;

import com.kkz.gmall.cart.client.CartFeignClient;
import com.kkz.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartDegradeFeignClient implements CartFeignClient {
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        return null;
    }
}
