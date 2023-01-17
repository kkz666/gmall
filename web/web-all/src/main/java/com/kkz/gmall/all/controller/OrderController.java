package com.kkz.gmall.all.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class OrderController {
    @Autowired
    private OrderFeignClient orderFeignClient;
    /**
     * 去结算
     * @return
     */
    @GetMapping("/trade.html")
    public String trade(Model model){
        Result<Map<String,Object>> trade = orderFeignClient.trade();
        //响应
        model.addAllAttributes(trade.getData());
        return "order/trade";
    }
    /**
     * 跳转我的订单
     * @return
     */
    @GetMapping("/myOrder.html")
    public String myOrder(){
        return "order/myOrder";
    }
}
