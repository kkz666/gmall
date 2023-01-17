package com.kkz.gmall.mq.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
