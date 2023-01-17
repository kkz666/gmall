package com.kkz.gmall.order.client;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-order", fallback = OrderDegradeFeignClient.class)
@Repository
public interface OrderFeignClient {
    /**
     *  /api/order/inner/getOrderInfo/{orderId}
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable Long orderId);
    /**
     * /api/order/auth/trade
     * 去结算
     * @return
     */
    @GetMapping("/api/order/auth/trade")
    Result trade();
}
