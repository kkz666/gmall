package com.kkz.gmall.payment.client.impl;

import com.kkz.gmall.model.payment.PaymentInfo;
import com.kkz.gmall.payment.client.PaymentFeignClient;
import org.springframework.stereotype.Component;

@Component
public class PaymentDegradeFeignClient implements PaymentFeignClient {
    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return null;
    }

    @Override
    public Boolean closePay(Long orderId) {
        return null;
    }

    @Override
    public Boolean checkPayment(Long orderId) {
        return null;
    }
}
