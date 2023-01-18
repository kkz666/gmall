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
}
