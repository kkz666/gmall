package com.kkz.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.model.enums.ProcessStatus;
import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.model.payment.PaymentInfo;
import com.kkz.gmall.order.service.OrderService;
import com.kkz.gmall.payment.client.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentFeignClient paymentFeignClient;
    /**
     * 订单超时，判断订单状态
     *  已支付，不进行操作
     *  未支付,修改状态
     * @param orderId
     * @param message
     * @param channel
     */
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    @SneakyThrows
    public void cancelOrderStatus(Long orderId, Message message, Channel channel){
        try {
            //判断
            if (orderId != null) {
                //查询订单，根据id
                //先写查询订单的service，调用
                //使用通用service- 继承接口 ，实现
                OrderInfo orderInfo = orderService.getById(orderId);
                //判断是否为空
                if (orderInfo != null) {
                    //获取状态进行判断
                    if ("UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())) {
                        // 查询支付记录信息
                        PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                        // 判断状态
                        if (paymentInfo != null && "UNPAID".equals(paymentInfo.getPaymentStatus())) {
                            // 查询支付宝记录
                            Boolean flag = this.paymentFeignClient.checkPayment(orderId);
                            if (flag) {
                                //有支付宝记录
                                Boolean result = this.paymentFeignClient.closePay(orderId);
                                // 关闭支付宝支付成功
                                if (result) {
                                    //设置2表示  关闭订单 关闭支付记录
                                    orderService.execExpiredOrder(orderId,"2");
                                }else{
                                    //有可能是已支付
                                }
                            } else {
                                orderService.execExpiredOrder(orderId,"2");
                            }
                        } else {
                            // 没有支付宝支付记录
                            // 调用接口关闭订单
                            // 处理超时订单
                            orderService.execExpiredOrder(orderId, "1");
                        }
                    }
                }
            }
        } catch (Exception e) {
            //写入日志，数据库，短信
            e.printStackTrace();
        }
        //手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,autoDelete = "false"),
            key ={MqConst.ROUTING_PAYMENT_PAY}
    ))
    @SneakyThrows
    public void paySuccess(Long orderId, Message message,Channel channel){
        try {
            // 判断
            if (orderId != null) {
                //查询状态
                OrderInfo orderInfo = this.orderService.getOrderInfoById(orderId);
                if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus())) {
                    //修改状态
                    this.orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                    //发送消息，扣减库存
                    this.orderService.sendOrderStatus(orderInfo);
                }
            }
        } catch (Exception e) {
            //日志，短信通知
            e.printStackTrace();
        }
        //手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    /**
     * 库存扣减成功，更新操作
     * @param strJson
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(value = MqConst.QUEUE_WARE_ORDER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER,autoDelete = "false"),
            key ={MqConst.ROUTING_WARE_ORDER}
    ))
    @SneakyThrows
    public void stockOrderStatus(String strJson ,Message message,Channel channel){
        try {
            //判断
            if (!StringUtils.isEmpty(strJson)) {
                //转换数据类型
                Map<String, String> map = JSON.parseObject(strJson, Map.class);
                //获取订单id
                String orderId = map.get("orderId");
                //获取状态
                String status = map.get("status");
                //判断是否扣减成功
                if ("DEDUCTED".equals(status)) {
                    // 待发货
                    this.orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
                } else {
                    // 库存异常
                    this.orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
                    //通过管理员，人工客服，商家
                }
            }
        } catch (NumberFormatException e) {
            //出现异常，邮件短信等通知
            e.printStackTrace();
        }
        //手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
