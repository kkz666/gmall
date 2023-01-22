package com.kkz.gmall.activity.service;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.activity.SeckillGoods;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface SeckillGoodsService {
    /**
     * 查询秒杀列表
     * @return
     */
    List<SeckillGoods> findAll();
    /**
     * 商品详情
     * @param skuId
     * @return
     */
    SeckillGoods getSeckillGoods(Long skuId);

    /**
     * 获取抢单码
     * @param skuId
     * @param request
     * @return
     */
    Result getSeckillSkuIdStr(Long skuId, HttpServletRequest request);

    /**
     * 秒杀下单
     * @param skuId
     * @param request
     * @return
     */
    Result seckillOrder(Long skuId,HttpServletRequest request);

    /**
     * 抢单
     * @param userId
     * @param skuId
     */
    void obtainOrder(String userId, Long skuId);
}
