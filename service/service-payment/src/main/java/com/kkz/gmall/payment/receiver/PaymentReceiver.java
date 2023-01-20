package com.kkz.gmall.payment.receiver;

import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.payment.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentReceiver {
    @Autowired
    private PaymentService paymentService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_CLOSE, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE, autoDelete = "false"),
            key = {MqConst.ROUTING_PAYMENT_CLOSE}
    ))
    @SneakyThrows
    public void closePayment(Long orderId, Message message, Channel channel){
        //判断
        if (orderId != null) {
            paymentService.closePayment(orderId);
        }
        //手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }
}
