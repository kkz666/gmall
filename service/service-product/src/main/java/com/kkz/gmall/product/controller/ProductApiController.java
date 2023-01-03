package com.kkz.gmall.product.controller;

import com.kkz.gmall.model.product.*;
import com.kkz.gmall.product.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product/inner")
public class ProductApiController {
    @Autowired
    private ManagerService managerService;
    /**
     * /api/product/inner/getSkuInfo/{skuId}
     * 根据skuId查询skuInfo信息和图片列表
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        return skuInfo;
    }
    /**
     *根据三级分类id获取分类信息
     * api/product/inner/getCategoryView/{category3Id}
     *
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        return managerService.getCategoryView(category3Id);
    }
    /**
     * api/product/inner/getSkuPrice/{skuId}
     * 根据skuId查询sku实时价格
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        return  managerService.getSkuPrice(skuId);
    }
    /**
     * 根据spuId,skuId 获取销售属性数据
     * api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        return managerService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }
    /**
     * api/product/inner/getSkuValueIdsMap/{spuId}
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        return managerService.getSkuValueIdsMap(spuId);
    }

    /**
     * 根据spuid查询海报集合数据
     * api/product/inner/findSpuPosterBySpuId/{spuId}
     * @param spuId
     * @return
     */
    @GetMapping("/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId){
        return managerService.findSpuPosterBySpuId(spuId);
    }
    /**
     *
     * 根据skuId 获取平台属性数据
     * api/product/inner/getAttrList/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        return managerService.getAttrList(skuId);
    }
}
