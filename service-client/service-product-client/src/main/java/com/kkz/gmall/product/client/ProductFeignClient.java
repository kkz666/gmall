package com.kkz.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.product.*;
import com.kkz.gmall.product.client.impl.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
//fallback降级处理
@FeignClient(value = "service-product", fallback = ProductDegradeFeignClient.class)
@Repository
public interface ProductFeignClient {
    /**
     * /api/product/inner/getSkuInfo/{skuId}
     * 根据skuId查询skuInfo信息和图片列表
     * @param skuId
     * @return
     */
    @GetMapping("api/product/inner/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable Long skuId);
    /**
     *根据三级分类id获取分类信息
     * api/product/inner/getCategoryView/{category3Id}
     *
     * @param category3Id
     * @return
     */
    @GetMapping("api/product/inner/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable Long category3Id);
    /**
     * api/product/inner/getSkuPrice/{skuId}
     * 根据skuId查询sku实时价格
     * @param skuId
     * @return
     */
    @GetMapping("api/product/inner/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable Long skuId);
    /**
     * 根据spuId,skuId 获取销售属性数据
     * api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId);
    /**
     * api/product/inner/getSkuValueIdsMap/{spuId}
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     */
    @GetMapping("api/product/inner/getSkuValueIdsMap/{spuId}")
    Map getSkuValueIdsMap(@PathVariable Long spuId);

    /**
     * 根据spuid查询海报集合数据
     * api/product/inner/findSpuPosterBySpuId/{spuId}
     * @param spuId
     * @return
     */
    @GetMapping("api/product/inner/findSpuPosterBySpuId/{spuId}")
    List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId);
    /**
     *
     * 根据skuId 获取平台属性数据
     * api/product/inner/getAttrList/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("api/product/inner/getAttrList/{skuId}")
    List<BaseAttrInfo> getAttrList(@PathVariable Long skuId);
    /**
     * 首页数据查询三级分类数
     * api/product/inner/getBaseCategoryList
     *
     * @return
     */
    @GetMapping("api/product/inner/getBaseCategoryList")
    Result getBaseCategoryList();
}
