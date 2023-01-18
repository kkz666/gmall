package com.kkz.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.kkz.gmall.model.enums.PaymentStatus;
import com.kkz.gmall.model.enums.PaymentType;
import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.model.payment.PaymentInfo;
import com.kkz.gmall.order.client.OrderFeignClient;
import com.kkz.gmall.payment.config.AlipayConfig;
import com.kkz.gmall.payment.service.AlipayService;
import com.kkz.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 支付宝下单
     *
     * @param orderId
     * @return
     */
    @Override
    public String submitOrder(Long orderId) {
        //查询订单
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        //判断
        if (orderInfo == null || !"UNPAID".equals(orderInfo.getOrderStatus())) {
            return "当前订单已经关闭或者已经支付";
        }
        // 保存支付信息
        this.paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());
        // 对接支付宝
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建API对应的request
        // 设置同步回调地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 异步回调地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址
        // 设置请求参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        // 总金额
        bizContent.put("total_amount", 0.01);
        // 订单标题
        bizContent.put("subject", orderInfo.getTradeBody());
        //设置超时时间--相对时间
//        bizContent.put("timeout_express", "10m");
//          设置超时时间--绝对时间
        //获取十分后的时间 yyyy-MM-dd HH:mm:ss
        Calendar calendar = Calendar.getInstance();
        //加上十分钟
        calendar.add(Calendar.MINUTE, 10);
        //获取时间
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
        bizContent.put("time_expire", format);
        // 产品码
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizContent(bizContent.toJSONString()); //填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

    /**
     * 发起退款！
     * @param orderId
     */
    @Override
    public boolean refund(Long orderId) {
        //查询根据orderId查询订单对象
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        //判断
        if (orderInfo == null || !"PAID".equals(orderInfo.getOrderStatus())) {
            return false;
        }
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        bizContent.put("refund_amount", 0.01);
        bizContent.put("refund_reason", "不想要了，帮忙退一下！！");
        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            // 订单 已支付--关闭
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
            // 支付记录
            this.paymentService.updatePaymentInfoStatus(orderInfo.getOutTradeNo(), PaymentType.ALIPAY.name(), paymentInfo);
            // TODO 订单状态--发送mq消息
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }
}
