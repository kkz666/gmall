package com.kkz.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kkz.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManagerService {

    /**
     * 查询一级分类
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类id查询二级分类数据
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级分类查询三级分类数据
     * @param category2Id
     */
    List<BaseCategory3>  getCategory3(Long category2Id);

    /**
     *
     *根据分类id查询平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 新增和修改平台属性
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据属性id查询属性对象
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(Long attrId);
    /**
     * 根据三级分类分页查询spu列表
     * @param spuInfo
     * @param infoPage
     * @return
     */
    IPage<SpuInfo> getSpuInfoPage(SpuInfo spuInfo, Page<SpuInfo> infoPage);

    /**
     *  获取销售属性
     * @return
     */
    List<BaseSaleAttr> baseSaleAttrList();
    /**
     * 保存spu
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId查询销售属性和销售属性值集合
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    /**
     * 根据spuId查询图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> spuImageList(Long spuId);

    /**
     * 保存skuInfo
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * sku分页列表
     * @param skuInfoPage
     * @return
     */
    IPage<SkuInfo> skuListPage(Page<SkuInfo> skuInfoPage);

    /**
     * 商品的上架
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 商品的下架
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 根据skuId查询skuInfo信息和图片列表
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据三级分类id获取分类信息
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryView(Long category3Id);

    /**
     * 根据skuId查询sku实时价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);
    /**
     * 根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);
    /**
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);
    /**
     * 根据spuid查询海报集合数据
     * @param spuId
     * @return
     */
    List<SpuPoster> findSpuPosterBySpuId(Long spuId);

    /**
     * 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);
    /**
     * 首页数据查询三级分类数
     * @return
     */
    List<JSONObject> getBaseCategoryList();
    /**
     * 通过品牌Id 来查询数据
     * @param tmId
     * @return
     */
    BaseTrademark getTrademarkByTmId(Long tmId);
}
