package com.kkz.gmall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kkz.gmall.cart.client.CartFeignClient;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.util.AuthContextHolder;
import com.kkz.gmall.model.cart.CartInfo;
import com.kkz.gmall.model.order.OrderDetail;
import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.order.service.OrderService;
import com.kkz.gmall.product.client.ProductFeignClient;
import com.kkz.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
public class OrderApiController {
    @Autowired
    private OrderService orderService;
    /**
     *  /api/order/inner/getOrderInfo/{orderId}
     * 根据订单Id 查询订单信息
     * @param oderId
     * @return
     */
    @GetMapping("/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        return orderService.getOrderInfoById(orderId);
    }
    /**
     * /api/order/auth/{page}/{limit}
     * 我的订单
     * @return
     */
    @GetMapping("/auth/{page}/{limit}")
    public Result getOrderPageByUserId(@PathVariable Long page,
                                       @PathVariable Long limit, HttpServletRequest request){
        IPage<OrderInfo> orderInfoIPage = orderService.getOrderPageByUserId(page, limit, request);
        return Result.ok(orderInfoIPage);
    }
    /**
     * /api/order/auth/trade
     * 结算
     * @return
     */
    @GetMapping("/auth/trade")
    public Result trade(HttpServletRequest request){
        return orderService.trade(request);
    }
    /**
     * api/order/auth/submitOrder
     * 提交订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request){
        return orderService.submitOrder(orderInfo, request);
    }

}
