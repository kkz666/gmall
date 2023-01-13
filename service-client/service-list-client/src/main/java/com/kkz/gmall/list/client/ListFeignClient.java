package com.kkz.gmall.list.client;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.list.client.impl.ListDegradeFeignClient;
import com.kkz.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-list", fallback = ListDegradeFeignClient.class)
@Repository
public interface ListFeignClient {
    /**
     * api/list
     * 商品搜索
     * @return
     */
    @PostMapping("/api/list")
    Result list(@RequestBody SearchParam searchParam);
    /**
     * api/list/inner/incrHotScore/{skuId}
     * 更新商品的热度排名
     * @param skuId
     * @return
     */
    @GetMapping("api/list/inner/incrHotScore/{skuId}")
    Result incrHotScore(@PathVariable Long skuId);
}
