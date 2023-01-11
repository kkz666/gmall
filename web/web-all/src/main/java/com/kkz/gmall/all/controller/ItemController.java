package com.kkz.gmall.all.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class ItemController {
    @Autowired
    private ItemFeignClient itemFeignClient;

    @GetMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) {
        // 获取数据
        Result<Map<String, Object>> item = itemFeignClient.getItem(skuId);
        //设置数据
        model.addAllAttributes(item.getData());
        return "item/item";
    }
}
