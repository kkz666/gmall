package com.kkz.gmall.cart.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kkz.gmall.model.cart.CartInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CartService {
    /**
     * 获取选中状态的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
    /**
     * 删除购物车
     * @param request
     * @param skuId
     */
    void deleteCart(HttpServletRequest request, Long skuId);
    /**
     * 更改选中状态
     * @param request
     * @param skuId
     * @param isChecked
     */
    void checkCart(HttpServletRequest request, Long skuId, Integer isChecked);
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
