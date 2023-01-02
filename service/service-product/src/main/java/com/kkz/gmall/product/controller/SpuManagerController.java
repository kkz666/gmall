package com.kkz.gmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.model.product.BaseSaleAttr;
import com.kkz.gmall.model.product.SpuInfo;
import com.kkz.gmall.product.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class SpuManagerController {
    @Autowired
    private ManagerService managerService;
    /**
     * admin/product/{page}/{limit}
     * 根据三级分类分页查询spu列表
     * @return
     */
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){
        Page<SpuInfo> infoPage = new Page<>(page, limit);
        IPage<SpuInfo> infoIPage = managerService.getSpuInfoPage(spuInfo, infoPage);
        return Result.ok(infoIPage);
    }
    /**
     * admin/product/baseSaleAttrList
     * 获取销售属性
     *
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList= managerService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }
    /**
     * admin/product/saveSpuInfo
     * 保存spu
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        managerService.saveSpuInfo(spuInfo);
        return Result.ok();
    }
}
