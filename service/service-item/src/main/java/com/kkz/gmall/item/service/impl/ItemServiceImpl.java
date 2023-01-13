package com.kkz.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.item.service.ItemService;
import com.kkz.gmall.list.client.ListFeignClient;
import com.kkz.gmall.model.product.*;
import com.kkz.gmall.product.client.ProductFeignClient;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private ListFeignClient listFeignClient;
    /**
     * 获取商品详情
     * @param skuId
     * @return
     */
    @Override
    public HashMap<String, Object> getItem(Long skuId) {
        HashMap<String, Object> resultMap = new HashMap<>();

//        // 布隆过滤器判断数据是否存在
//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//        // 没有当前sku
//        if (!bloomFilter.contains(skuId)) {
//            return resultMap;
//        }
        // supplyAsync有返回值
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                //获取sku信息和图片列表
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                resultMap.put("skuInfo", skuInfo);
                return skuInfo;
            }
        }, executor);
        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                // 获取实时价格
                BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
                resultMap.put("price", skuPrice);
            }
        }, executor);
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                // 获取三级分类
                BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                resultMap.put("categoryView", categoryView);
            }
        }, executor);
        CompletableFuture<Void> spuSaleAttrListCheckBySkuCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                // 获取销售属性和选中状态
                List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
                resultMap.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
            }
        }, executor);
        CompletableFuture<Void> skuValueIdsMapCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                // 获取商品切换数据
                Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
                resultMap.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
            }
        }, executor);
        CompletableFuture<Void> findSpuPosterBySpuIdCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                // 获取海报信息
                List<SpuPoster> spuPosterBySpuId = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
                resultMap.put("spuPosterList", spuPosterBySpuId);
            }
        }, executor);
        CompletableFuture<Void> attrListCompletableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                // 获取平台信息
                List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
                // 处理符合要求 List  Obj  key attrName   key attrValue
                List<Map<String, String>> spuAttrList = attrList.stream().map(baseAttrInfo -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("attrName", baseAttrInfo.getAttrName());
                    map.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return map;
                }).collect(Collectors.toList());
                resultMap.put("skuAttrList", spuAttrList);
            }
        }, executor);
        // 更新商品热度
        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                listFeignClient.incrHotScore(skuId);
            }
        }, executor);
        // 所有的异步执行完成之后才算完成
        CompletableFuture.allOf(skuInfoCompletableFuture, skuPriceCompletableFuture, categoryViewCompletableFuture,
                spuSaleAttrListCheckBySkuCompletableFuture, skuValueIdsMapCompletableFuture, findSpuPosterBySpuIdCompletableFuture,
                attrListCompletableFuture, incrHotScoreCompletableFuture).join();
        return resultMap;
    }
}
