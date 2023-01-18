package com.kkz.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.enums.PaymentType;
import com.kkz.gmall.model.payment.PaymentInfo;
import com.kkz.gmall.payment.config.AlipayConfig;
import com.kkz.gmall.payment.service.AlipayService;
import com.kkz.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/api/payment/alipay")
public class PaymentApiController {
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 支付宝下单
     * http://api.gmall.com/api/payment/alipay/submit/99
     *
     * @param orderId
     * @return
     */
    @GetMapping("/submit/{orderId}")
    @ResponseBody
    public String submitOrder(@PathVariable Long orderId){
        String form = alipayService.submitOrder(orderId);
        return form;
    }
    /**
     * 同步回调,处理
     * @return
     * http://api.gmall.com/api/payment/alipay/callback/return
     */
    @RequestMapping("/callback/return")
    public String returnCallback(){
        return "redirect:" + AlipayConfig.return_order_url;
    }
    /**
     * http://vguqe8.natappfree.cc/api/payment/alipay/callback/notify
     * 异步回调
     * @return
     */
    @PostMapping("/callback/notify")
    public String notifyCallBakc(@RequestParam Map<String, String> paramsMap) {
        //实现验签
        //调用SDK验证签名
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key,
                    AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //获取订单交易号
        String outTradeNo = paramsMap.get("out_trade_no");
        //获取总金额
        String totalAmount = paramsMap.get("total_amount");
        //获取appId
        String appId = paramsMap.get("app_id");
        //获取当前支付的状态
        String tradeStatus = paramsMap.get("trade_status");
        //获取notify_id
        String notifyId = paramsMap.get("notify_id");
        if (signVerified) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，
            //  对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，
            //  校验失败返回failure
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
            //二次校验,校验appid，订单金额，传给支付宝的订单交易号
            if (paymentInfo != null && new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount)) == 0 &&
                    AlipayConfig.app_id.equals(appId)) {
                //设置支付记录状态
                //只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 存储notify_id保证，异步处理的幂等性
                    Boolean flag = this.redisTemplate.opsForValue().setIfAbsent(notifyId, notifyId, 1462,
                            TimeUnit.MINUTES);
                    if (flag) {
                        paymentService.updatePaymentInfo(outTradeNo, PaymentType.ALIPAY.name(), paramsMap);
                        return "success";
                    } else {
                        return "success";
                    }
                }
            }
        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }
    /**
     * 发起退款！
     * http://localhost:8205/api/payment/alipay/refund/20
     * @return
     */
    @GetMapping("/refund/{orderId}")
    @ResponseBody
    public Result refund(@PathVariable Long orderId){
        boolean flag = alipayService.refund(orderId);
        return Result.ok(flag);
    }
}
