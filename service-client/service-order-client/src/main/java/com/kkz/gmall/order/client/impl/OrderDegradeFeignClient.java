package com.kkz.gmall.order.client.impl;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

@Component
public class OrderDegradeFeignClient implements OrderFeignClient {

    @Override
    public Result trade() {
        return null;
    }
}
