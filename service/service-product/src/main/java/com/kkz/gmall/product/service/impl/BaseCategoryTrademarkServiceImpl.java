package com.kkz.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kkz.gmall.model.product.BaseCategoryTrademark;
import com.kkz.gmall.model.product.BaseTrademark;
import com.kkz.gmall.model.product.CategoryTrademarkVo;
import com.kkz.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.kkz.gmall.product.mapper.BaseTrademarkMapper;
import com.kkz.gmall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;
    /**
     * 根据category3Id获取可选品牌列表
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        List<BaseTrademark> baseTrademarkListResult = new ArrayList<>();
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        // 根据三级列表获取到的品牌id列表
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        // 处理获取品牌id
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            List<Long> trademarkIdList = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {
                // 遍历后返回品牌id
                return baseCategoryTrademark.getTrademarkId();
            }).collect(Collectors.toList());

            // 方式一：
            // 查询所有品牌
//            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null);
//            baseTrademarkListResult = baseTrademarkList.stream().filter(baseTrademark -> {
//                // 返回不包含在查出来已关联的品牌
//                return !trademarkIdList.contains(baseTrademark.getId());
//            }).collect(Collectors.toList());

            // 方式二：
            //创建条件对象
            QueryWrapper<BaseTrademark> queryWrapper = new QueryWrapper<>();
            queryWrapper.notIn("id", trademarkIdList);
            //条件查询品牌列表
            baseTrademarkListResult=  baseTrademarkMapper.selectList(queryWrapper);
        }
        return baseTrademarkListResult;
    }
    /**
     * 根据category3Id获取品牌列表
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        List<BaseTrademark> baseTrademarkList = new ArrayList<>();
        QueryWrapper<BaseCategoryTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", category3Id);
        // 根据三级列表获取到的品牌id列表
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(queryWrapper);

        // 处理获取品牌id
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            List<Long> trademarkIdList = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {
                // 遍历后返回品牌id
                return baseCategoryTrademark.getTrademarkId();
            }).collect(Collectors.toList());
            // 根据品牌id集合查询品牌数据
            // 方式一：
//            for (Long id : trademarkIdList) {
//                BaseTrademark baseTrademark = baseTrademarkMapper.selectById(id);
//                baseTrademarkList.add(baseTrademark);
//            }
            // 方式二：
            QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper=new QueryWrapper<>();
            baseTrademarkQueryWrapper.in("id",trademarkIdList);
            baseTrademarkList = baseTrademarkMapper.selectList(baseTrademarkQueryWrapper);
        }
        return baseTrademarkList;
    }
    /**
     * 删除分类品牌关联
     * @param category3Id
     * @param trademarkId
     */
    @Override
    public void remove(Long category3Id, Long trademarkId) {
        QueryWrapper<BaseCategoryTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);
        queryWrapper.eq("trademark_id",trademarkId);
        //删除关联
        baseCategoryTrademarkMapper.delete(queryWrapper);
    }

    /**
     * 保存分类和品牌关联
     * @param categoryTrademarkVo
     *  61 5,7
     *
     *  61 5  对象 BaseCategoryTrademark
     *  61 7  对象 BaseCategoryTrademark
     *
     */
    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        //获取品牌id集合
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        //遍历处理-封装成对象
        List<BaseCategoryTrademark> baseCategoryTrademarkList = trademarkIdList.stream().map(trademarkId -> {
            //创建分类品牌对象
            BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
            baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
            baseCategoryTrademark.setTrademarkId(trademarkId);
            return baseCategoryTrademark;
        }).collect(Collectors.toList());
        this.saveBatch(baseCategoryTrademarkList);
    }
}
