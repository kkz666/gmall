package com.kkz.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    /**
     * 跳转到登录页面
     *
     * @param originUrl
     * @return
     */
    @GetMapping("/login.html")
    public String toLogin(String originUrl, Model model) {
        model.addAttribute("originUrl", originUrl);
//        System.out.println(originUrl);
        return "login";
    }
}
