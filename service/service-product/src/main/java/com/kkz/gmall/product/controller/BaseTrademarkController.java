package com.kkz.gmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.product.BaseTrademark;
import com.kkz.gmall.product.service.BaseTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /**
     * admin/product/baseTrademark/{page}/{limit}
     * 品牌分页列表查询
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("{page}/{limit}")
    public Result getBaseTrademakrPage(@PathVariable Long page,
                                       @PathVariable Long limit){
        //封装分页对象
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page,limit);

        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.getBaseTrademakrPage(baseTrademarkPage);

        return Result.ok(baseTrademarkIPage);
    }
    /**
     * 保存
     * admin/product/baseTrademark/save
     * @param baseTrademark
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }
    /**
     * 修改品牌
     * admin/product/baseTrademark/update
     * @param baseTrademark
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }
    /**
     * admin/product/baseTrademark/get/{id}
     * 根据品牌Id 回显品牌数据 --- 获取详情
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }
    /**
     * admin/product/baseTrademark/remove/{id}
     * 根据id删除品牌
     * @param id
     * @return
     */
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
}
