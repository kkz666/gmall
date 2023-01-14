package com.kkz.gmall.cart.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kkz.gmall.model.cart.CartInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CartService {
    /**
     * 加入购物车
     */
    void addToCart(Long skuId, Integer skuNum, HttpServletRequest request);
    /**
     * 展示购物车
     * @param request
     * @return
     */
    List<CartInfo> cartList(HttpServletRequest request);
}
