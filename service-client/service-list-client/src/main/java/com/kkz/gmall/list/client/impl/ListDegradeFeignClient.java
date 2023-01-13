package com.kkz.gmall.list.client.impl;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.list.client.ListFeignClient;
import com.kkz.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;

@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result list(SearchParam searchParam) {
        return Result.fail();
    }

    @Override
    public Result incrHotScore(Long skuId) {
        return Result.fail();
    }
}
