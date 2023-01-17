package com.kkz.gmall.order.receiver;

import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.model.enums.ProcessStatus;
import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;
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
                        //调用接口关闭订单
                        //处理超时订单
                        orderService.execExpiredOrder(orderId);
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
}
