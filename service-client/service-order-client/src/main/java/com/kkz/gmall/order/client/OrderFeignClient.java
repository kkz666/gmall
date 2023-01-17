package com.kkz.gmall.order.client;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
@FeignClient(value = "service-order", fallback = OrderDegradeFeignClient.class)
@Repository
public interface OrderFeignClient {
    /**
     * /api/order/auth/trade
     * 去结算
     * @return
     */
    @GetMapping("/api/order/auth/trade")
    Result trade();
}
