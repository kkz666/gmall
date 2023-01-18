package com.kkz.gmall.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {
    //支付宝网关
    @Value("${alipay_url}")
    private String alipay_url;
    //应用id
    public static String app_id;
    @Value("${app_id}")
    public void setApp_id(String app_id) {
        AlipayConfig.app_id = app_id;
    }
    //应用私钥
    @Value("${app_private_key}")
    private String app_private_key;
    //数据格式
    public final  static String  format = "json";
    //字符编码集
    public final static String charset = "UTF-8";
    //支付宝公钥
    public static String alipay_public_key;
    @Value("${alipay_public_key}")
    public void setAlipay_public_key(String alipay_public_key) {
        AlipayConfig.alipay_public_key = alipay_public_key;
    }
    //签名算法
    public final static String sign_type ="RSA2";
    public static String return_payment_url;
    @Value("${return_payment_url}")
    public void setReturn_payment_url(String return_payment_url) {
        AlipayConfig.return_payment_url = return_payment_url;
    }
    public static String return_order_url;

    @Value("${return_order_url}")
    public void setReturn_order_url(String return_order_url) {
        AlipayConfig.return_order_url = return_order_url;
    }
    public static String notify_payment_url;
    @Value("${notify_payment_url}")
    public void setNotify_payment_url(String notify_payment_url) {
        AlipayConfig.notify_payment_url = notify_payment_url;
    }
    @Bean
    public AlipayClient alipayClient() {
        AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, app_id, app_private_key, format,charset, alipay_public_key, sign_type);
        return alipayClient;
    }
}
