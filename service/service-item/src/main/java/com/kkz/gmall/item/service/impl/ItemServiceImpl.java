package com.kkz.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.kkz.gmall.item.service.ItemService;
import com.kkz.gmall.model.product.*;
import com.kkz.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeignClient productFeignClient;
    /**
     * 获取商品详情
     * @param skuId
     * @return
     */
    @Override
    public HashMap<String, Object> getItem(Long skuId) {
        HashMap<String, Object> resultMap = new HashMap<>();
        // 获取sku信息和图片列表
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        resultMap.put("skuInfo", skuInfo);
        // 获取实时价格
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        resultMap.put("price", skuPrice);
        // 判断
        if(skuInfo != null){
            // 获取三级分类
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            resultMap.put("categoryView", categoryView);
            // 获取销售属性和选中状态
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId,skuInfo.getSpuId());
            resultMap.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
            // 获取商品切换数据
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            resultMap.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
            // 获取海报信息
            List<SpuPoster> spuPosterBySpuId = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            resultMap.put("spuPosterList", spuPosterBySpuId);
        }
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
        return resultMap;
    }
}
