package com.kkz.gmall.mq.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.mq.config.DeadLetterMqConfig;
import com.kkz.gmall.mq.config.DelayedMqConfig;
import com.kkz.gmall.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/mq")
public class MqController {
    @Autowired
    private RabbitService rabbitService;
    /**
     * 发送消息的方法
     * @return
     */
    @GetMapping("/send")
    public Result send(){
        rabbitService.sendMessage("exchange.confirm", "routingKey.confirm888", "你好，我是消息，我来了");
        return Result.ok();
    }
    /**
     * 发送延迟消息-死信队列
     * @return
     */
    @GetMapping("/sendDeaLetter")
    public Result sendDeaLetter(){
        //时间格式化
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        rabbitService.sendMessage(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, "我是延迟消息");
        System.out.println("消息发送的时间：\t" + simpleDateFormat.format(new Date()));
        return Result.ok();
    }
    @GetMapping("/sendDelayed")
    public  Result sendDelayed(){
        //时间格式化
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        //封装后的api
        rabbitService.sendDelayedMessage(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "我是延迟插件的消息", 10);
        System.out.println("延迟插件消息发送时间：\t" + simpleDateFormat.format(new Date()));
//       this.rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay,
//               "我是延迟插件的消息", new MessagePostProcessor() {
//                   @Override
//                   public Message postProcessMessage(Message message) throws AmqpException {
//                       //设置消息的延迟时间
//                       message.getMessageProperties().setDelay(10 * 1000);
//                       System.out.println("延迟插件消息发送时间：\t"+simpleDateFormat.format(new Date()));
//                         返回要放行的消息
//                       return message;
//                   }
//               });
        return Result.ok();
    }
}
