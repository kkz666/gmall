package com.kkz.gmall.item.client;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "service-item", fallback = ItemDegradeFeignClient.class)
@Repository
public interface ItemFeignClient {
    /**
     * 获取商品详情数据
     *  api/item/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("/api/item/{skuId}")
    Result getItem(@PathVariable Long skuId);
}
