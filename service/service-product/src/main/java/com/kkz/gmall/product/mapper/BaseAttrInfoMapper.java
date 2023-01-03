package com.kkz.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kkz.gmall.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     * 根据分类id查询平台属性集合
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoList(@Param("category1Id") Long category1Id,
                                          @Param("category2Id") Long category2Id,
                                          @Param("category3Id") Long category3Id);

    /**
     * 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> selectAttrList(Long skuId);
}
