package com.kkz.gmall.activity.client.impl;

import com.kkz.gmall.activity.client.ActivityFeignClient;
import com.kkz.gmall.common.result.Result;
import org.springframework.stereotype.Component;

@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {
    @Override
    public Result findAll() {
        return Result.fail();
    }

    @Override
    public Result getSeckillGoods(Long skuId) {
        return Result.fail();
    }
}
