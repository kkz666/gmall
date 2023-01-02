package com.kkz.gmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kkz.gmall.model.product.BaseTrademark;

public interface BaseTrademarkService extends IService<BaseTrademark> {
    /**
     * 品牌分页列表查询
     * @param baseTrademarkPage
     * @return
     */
    IPage<BaseTrademark> getBaseTrademakrPage(Page<BaseTrademark> baseTrademarkPage);
}
