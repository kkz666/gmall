package com.kkz.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kkz.gmall.model.product.SpuSaleAttr;
import com.kkz.gmall.model.product.SpuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpuSaleAttrValueMapper extends BaseMapper<SpuSaleAttrValue> {
}
