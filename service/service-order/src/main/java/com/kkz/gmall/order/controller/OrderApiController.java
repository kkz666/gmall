package com.kkz.gmall.order.controller;

import com.alibaba.fastjson.JSON;
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
import java.util.ArrayList;
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
     * //http://localhost:8204/api/order/orderSplit
     * 拆单实现
     * @return
     *
     * [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
     */
    @PostMapping("/orderSplit")
    public String orderSplit(HttpServletRequest request) {
        // 获取订单id
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        // 调用service处理拆单
        List<OrderInfo> orderInfoList = this.orderService.orderSplit(orderId,wareSkuMap);
        // 创建集合封装数据
        List<Map<String, Object>> resultMap = new ArrayList<>();
        // 遍历处理
        if (!CollectionUtils.isEmpty(orderInfoList)) {
            for (OrderInfo orderInfo : orderInfoList) {
                Map<String, Object> orderMap = this.orderService.initWareOrderMap(orderInfo);
                resultMap.add(orderMap);
            }
        }
        return JSON.toJSONString(resultMap);
    }
    /**
     *  /api/order/inner/getOrderInfo/{orderId}
     * 根据订单Id 查询订单信息
     * @param orderId
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
