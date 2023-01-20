package com.kkz.gmall.payment.client;

import com.kkz.gmall.model.payment.PaymentInfo;
import com.kkz.gmall.payment.client.impl.PaymentDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Repository
@FeignClient(value = "service-payment", fallback = PaymentDegradeFeignClient.class)
public interface PaymentFeignClient {
    /**
     * 查询支付记录信息
     * @param outTradeNo
     * @return
     */
    @GetMapping("/api/payment/alipay/getPaymentInfo/{outTradeNo}")
    @ResponseBody
    PaymentInfo getPaymentInfo(@PathVariable String outTradeNo);

    /**
     * http://localhost:8205/api/payment/alipay/closePay/25
     * 支付宝关闭交易
     * @param orderId
     * @return
     */
    @GetMapping("/api/payment/alipay/closePay/{orderId}")
    @ResponseBody
    Boolean closePay(@PathVariable Long orderId);
    /**
     * http://localhost:8205/api/payment/alipay/checkPayment/30
     * 查询支付宝交易记录
     * @param orderId
     * @return
     */
    @GetMapping("/api/payment/alipay/checkPayment/{orderId}")
    @ResponseBody
    Boolean checkPayment(@PathVariable Long orderId);
}
