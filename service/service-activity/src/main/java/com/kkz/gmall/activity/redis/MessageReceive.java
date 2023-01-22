package com.kkz.gmall.activity.redis;

import com.kkz.gmall.activity.util.CacheHelper;
import org.springframework.stereotype.Component;

@Component
public class MessageReceive {
    public void receiveMessage(String message){
        System.out.println(message);
        //去掉双引号
        message = message.replaceAll("\"", "");
        //截取
        String[] split = message.split(":");
        //判断
        if (split != null && split.length == 2) {
            CacheHelper.put(split[0], split[1]);
        }
    }
}
