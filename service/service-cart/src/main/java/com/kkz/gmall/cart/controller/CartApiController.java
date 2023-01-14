package com.kkz.gmall.cart.controller;

import com.kkz.gmall.cart.service.CartService;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.util.AuthContextHolder;
import com.kkz.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    @Autowired
    private CartService cartService;

    /**
     * /api/cart/addToCart/{skuId}/{skuNum}
     * 加入购物车
     * @return
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){
        //加入购物车
        cartService.addToCart(skuId, skuNum, request);
        return Result.ok();
    }
    /**
     * api/cart/cartList
     * 展示购物车
     * @return
     */
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request){
        //查询购物车
        List<CartInfo> cartInfoList = cartService.cartList(request);
        return Result.ok(cartInfoList);
    }
}
