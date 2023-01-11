package com.kkz.gmall.all.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;

@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;
    /**
     * 第一种方式：页面模板渲染
     * @return
     */
    @GetMapping({"/","index.html"})
    public String index(Model model){
        //远程获取三级分类的数据
        Result result = productFeignClient.getBaseCategoryList();
        //远程请求广告。。。。
        //设置数据
        model.addAttribute("list",result.getData());
        return "index/index";
    }
    /**
     * 第二种方式：生成页面-nginx做静态代理
     *    nginx:
     *          反向代理
     *         负载均衡
     *         动静分离---
     * @return
     */
    @GetMapping("/createIndex")
    @ResponseBody
    public Result createIndex(){
        //远程获取三级分类的数据
        Result result = productFeignClient.getBaseCategoryList();
        //创建对象封装数据
        Context context = new Context();
        //设置数据
        context.setVariable("list", result.getData());
        //声明输入位置
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("F:\\aaa\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //生成页面
        /**
         * 参数一：模板的位置 使用哪个模板
         * 参数二：数据，上下文对象
         * 参数三：生成页面的位置 流对象
         */
        templateEngine.process("index/index.html", context, fileWriter);
        return Result.ok();
    }
}
