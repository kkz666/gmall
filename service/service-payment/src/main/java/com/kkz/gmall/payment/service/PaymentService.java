package com.kkz.gmall.payment.service;

import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    /**
     * 保存支付信息
     * @param orderInfo
     * @param name
     */
    void savePaymentInfo(OrderInfo orderInfo, String name);
    /**
     * 查询支付记录
     * @param outTradeNo
     * @param name
     * @return
     */
    PaymentInfo getPaymentInfo(String outTradeNo, String name);
    /**
     * 修改支付记录状态
     * @param outTradeNo
     * @param name
     */
    void updatePaymentInfo(String outTradeNo, String name, Map<String, String> paramsMap);
    /**
     * 修改支付记录状态
     * @param outTradeNo
     * @param name
     * @param paymentInfo
     */
    void updatePaymentInfoStatus(String outTradeNo, String name, PaymentInfo paymentInfo);
}
