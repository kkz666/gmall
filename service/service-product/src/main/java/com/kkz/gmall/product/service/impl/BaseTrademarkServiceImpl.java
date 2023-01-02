package com.kkz.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kkz.gmall.model.product.BaseTrademark;
import com.kkz.gmall.product.mapper.BaseTrademarkMapper;
import com.kkz.gmall.product.service.BaseTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService{
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 品牌分页列表查询（无查询条件）
     * @param baseTrademarkPage
     * @return
     */
    @Override
    public IPage<BaseTrademark> getBaseTrademakrPage(Page<BaseTrademark> baseTrademarkPage) {
        //排序
        QueryWrapper<BaseTrademark> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByAsc("id");

        return baseTrademarkMapper.selectPage(baseTrademarkPage,queryWrapper);
    }
}
