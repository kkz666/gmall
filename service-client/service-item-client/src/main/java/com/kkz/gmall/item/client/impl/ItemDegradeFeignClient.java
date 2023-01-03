package com.kkz.gmall.item.client.impl;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result getItem(Long skuId) {
        return Result.fail();
    }
}
