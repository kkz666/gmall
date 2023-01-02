package com.kkz.gmall.product.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.product.BaseTrademark;
import com.kkz.gmall.model.product.CategoryTrademarkVo;
import com.kkz.gmall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/baseCategoryTrademark")
public class BaseCategoryTradeMarkController {
    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;
    /**
     * admin/product/baseCategoryTrademark/findTrademarkList/{category3Id}
     * 根据category3Id获取品牌列表
     * @param category3Id
     * @return
     */
    @GetMapping("/findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> baseTrademarkList= baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }
    /**
     * admin/product/baseCategoryTrademark/findCurrentTrademarkList/{category3Id}
     * 根据category3Id获取可选品牌列表
     * @param category3Id
     * @return
     */
    @GetMapping("/findCurrentTrademarkList/{category3Id}")
    public Result findCurrentTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }
    /**
     * admin/product/baseCategoryTrademark/remove/{category3Id}/{trademarkId}
     * 删除分类品牌关联
     * @param category3Id
     * @param trademarkId
     * @return
     */
    @DeleteMapping("/remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable Long category3Id,
                         @PathVariable Long trademarkId ){
        baseCategoryTrademarkService.remove(category3Id,trademarkId);
        return Result.ok();
    }
    /**
     * admin/product/baseCategoryTrademark/save
     * 保存分类和品牌关联
     * @param categoryTrademarkVo
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        baseCategoryTrademarkService.save(categoryTrademarkVo);
        return Result.ok();
    }
}
