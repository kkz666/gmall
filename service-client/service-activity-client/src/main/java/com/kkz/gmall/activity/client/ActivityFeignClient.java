package com.kkz.gmall.activity.client;

import com.kkz.gmall.activity.client.impl.ActivityDegradeFeignClient;
import com.kkz.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Repository
@FeignClient(value = "service-activity" ,fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {
    /**
     * api/activity/seckill/findAll
     * 查询秒杀列表
     * @return
     */
    @GetMapping("api/activity/seckill/findAll")
    Result findAll();

    /**
     *  /api/activity/seckill/getSeckillGoods/{skuId}
     * 获取秒杀商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    Result getSeckillGoods(@PathVariable Long skuId);
}
