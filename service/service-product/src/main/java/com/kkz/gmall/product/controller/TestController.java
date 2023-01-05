package com.kkz.gmall.product.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/product/test")
public class TestController {
    @Autowired
    private TestService testService;
    @GetMapping("/testLock")
    public Result testLock(){
        testService.testLock();
        return Result.ok();
    }
    @GetMapping("/read")
    public Result read(){
        String msg = testService.readLock();
        return Result.ok(msg);
    }

    @GetMapping("/write")
    public Result write(){
        String msg = testService.writeLock();
        return Result.ok(msg);
    }
}
