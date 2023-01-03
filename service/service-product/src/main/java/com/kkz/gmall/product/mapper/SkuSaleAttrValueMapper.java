package com.kkz.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kkz.gmall.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     *根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     * @return
     */
    List<Map> selectSkuValueIdsMap(Long spuId);
}
