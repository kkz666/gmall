package com.kkz.gmall.activity.controller;

import com.kkz.gmall.activity.service.SeckillGoodsService;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsController {
    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     *  /api/activity/seckill/auth/seckillOrder/{skuId}
     *  秒杀下单
     *
     * @return
     */
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId,HttpServletRequest request){
        return seckillGoodsService.seckillOrder(skuId, request);
    }
    /**
     * api/activity/seckill/findAll
     * 查询秒杀列表
     *
     * @return
     */
    @GetMapping("/findAll")
    public Result findAll() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.findAll();
        return Result.ok(seckillGoodsList);
    }
    /**
     *  /api/activity/seckill/getSeckillGoods/{skuId}
     * 获取秒杀商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId) {
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        return Result.ok(seckillGoods);
    }
    /**
     *  /api/activity/seckill/auth/getSeckillSkuIdStr/{skuId}
     * 获取抢单码
     * @return
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){
        return seckillGoodsService.getSeckillSkuIdStr(skuId, request);
    }
}
