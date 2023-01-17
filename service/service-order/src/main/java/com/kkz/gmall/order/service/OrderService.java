package com.kkz.gmall.order.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.order.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

import javax.servlet.http.HttpServletRequest;

public interface OrderService extends IService<OrderInfo> {
    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    Long saveOrder(OrderInfo orderInfo);
    /**
     * 提交订单
     * @param orderInfo
     * @param request
     * @return
     */
    Result submitOrder(OrderInfo orderInfo, HttpServletRequest request);

    /**
     * 结算
     * @param request
     * @return
     */
    Result trade(HttpServletRequest request);
    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);
    /**
     * 校验流水号
     * @param userId
     * @param tradeNoCode
     * @return
     */
    boolean checkTradeCode(String userId,String tradeNoCode);
    /**
     * 删除流水号
     * @param userId
     */
    void deleteTradeCode(String userId);
    /**
     * 校验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId,String skuNum);

    /**
     * 我的订单
     * @param page
     * @param limit
     * @param request
     * @return
     */
    IPage<OrderInfo> getOrderPageByUserId(Long page, Long limit, HttpServletRequest request);
    /**
     * 处理超时订单
     * @param orderId
     */
    void execExpiredOrder(Long orderId);
    /**
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoById(Long orderId);
}
