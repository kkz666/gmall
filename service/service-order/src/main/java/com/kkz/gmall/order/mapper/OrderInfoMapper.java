package com.kkz.gmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkz.gmall.model.order.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    /**
     * 我的订单
     * @param orderInfoPage
     * @param userId
     * @return
     */
    IPage<OrderInfo> selectOrderPageByUserId(Page<OrderInfo> orderInfoPage, @Param("userId") String userId);
}
