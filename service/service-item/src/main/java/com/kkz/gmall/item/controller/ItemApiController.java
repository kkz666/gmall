package com.kkz.gmall.item.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/item/")
public class ItemApiController {
    @Autowired
    private ItemService itemService;

    /**
     * 获取商品详情数据
     *  api/item/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId){
        HashMap<String,Object> resultMap = itemService.getItem(skuId);
        return Result.ok(resultMap);
    }
}
