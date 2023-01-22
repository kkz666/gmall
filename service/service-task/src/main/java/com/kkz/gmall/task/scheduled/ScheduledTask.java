package com.kkz.gmall.task.scheduled;

import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 开启定时任务支持
@EnableScheduling
@Component
@Slf4j
public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;
    /**
     * 参数值：
     *  秒 分 时 日 月 星期 年（不写）
     *   *：任何时间
     *   日期和星期互斥使用问号占位
     *   0/15:从0秒开始每15秒执行一次
     */
    @Scheduled(cron = "0/15 * * * * ? ")
    public void tesk1(){
//        System.out.println("执行了");
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_1, "");
    }
}
