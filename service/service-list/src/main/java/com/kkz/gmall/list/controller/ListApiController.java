package com.kkz.gmall.list.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/list")
public class ListApiController {
    ///api/list/createIndex
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    /**
     * 创建索引库，建立mapping结构
     * @return
     */
    @GetMapping("createIndex")
    public Result createIndex(){
        //创建索引库
        restTemplate.createIndex(Goods.class);
        //建立mapping
        restTemplate.putMapping(Goods.class);
        return Result.ok();

    }
}
