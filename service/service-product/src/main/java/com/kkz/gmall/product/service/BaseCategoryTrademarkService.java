package com.kkz.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kkz.gmall.model.product.BaseCategoryTrademark;
import com.kkz.gmall.model.product.BaseTrademark;
import com.kkz.gmall.model.product.CategoryTrademarkVo;

import java.util.List;

public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    /**
     * 根据category3Id获取可选品牌列表
     * @param category3Id
     * @return
     */

    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);
    /**
     * 根据category3Id获取品牌列表
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);

    /**
     * 删除分类品牌关联
     * @param category3Id
     * @param trademarkId
     */
    void remove(Long category3Id, Long trademarkId);

    /**
     * 保存分类和品牌关联
     * @param categoryTrademarkVo
     */
    void save(CategoryTrademarkVo categoryTrademarkVo);
}
