package com.kkz.gmall.payment.service;

public interface AlipayService {
    /**
     * 支付宝下单
     * @param orderId
     * @return
     */
    String submitOrder(Long orderId);

    /**
     * 发起退款！
     * @param orderId
     */
    boolean refund(Long orderId);
    /**
     * 查询支付宝交易记录
     * @param orderId
     * @return
     */
    Boolean checkPayment(Long orderId);
    /**
     * 支付宝关闭交易
     * @param orderId
     * @return
     */
    Boolean closePay(Long orderId);
}
