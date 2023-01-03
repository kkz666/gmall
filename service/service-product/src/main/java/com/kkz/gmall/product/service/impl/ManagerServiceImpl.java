package com.kkz.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkz.gmall.model.product.*;
import com.kkz.gmall.product.mapper.*;
import com.kkz.gmall.product.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerServiceImpl implements ManagerService {
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuPosterMapper spuPosterMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        // 查询所有
        List<BaseCategory1> baseCategory1s = baseCategory1Mapper.selectList(null);
        return baseCategory1s;
    }

    /**
     * 根据一级分类id查询二级分类数据
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //select *from base_category2 where category1_id=category1Id
        //创建查询条件
        QueryWrapper<BaseCategory2> queryWrapper=new QueryWrapper<>();
        //添加条件
        queryWrapper.eq("category1_id",category1Id);
        //查询结果
        return baseCategory2Mapper.selectList(queryWrapper);
    }

    /**
     * 根据二级分类查询三级分类数据
     * @param category2Id
     */
    @Override
    public  List<BaseCategory3>  getCategory3(Long category2Id) {
        //select *from base_category3 where category2_id=category1Id
        //创建查询条件
        QueryWrapper<BaseCategory3> queryWrapper=new QueryWrapper<>();
        //添加条件
        queryWrapper.eq("category2_id",category2Id);
        //查询结果
        return baseCategory3Mapper.selectList(queryWrapper);
    }
    /**
     * 根据分类id查询平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //调用mapper查询
        return  baseAttrInfoMapper.selectAttrInfoList(category1Id,category2Id,category3Id);
    }

    /**
     * 新增和修改平台属性
     * @param baseAttrInfo
     *
     *  Transactional：
     *      默认配置的方式：只能对运行时异常进行回滚
     *       RuntimeException
     *
     *  rollbackFor = Exception.class
     *    IOException
     *    SQLException
     *
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断当前操作是否保存还是修改
        if(baseAttrInfo.getId() != null){
            //修改平台属性
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //根据平台属性删除属性值集合 逻辑删除 ，sql文件物理删除
            //UPDATE base_attr_value SET is_deleted=1 WHERE is_deleted=0 AND (attr_id = ?)
            //创建删除条件对象
            QueryWrapper<BaseAttrValue> wrapper=new QueryWrapper<>();
            wrapper.eq("attr_id",baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);
        }else{
            //保存平台属性
            // 保存成功之后mp自动将自增的id填充进去
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //操作平台属性值
        //新增，获取平台属性值集合
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //判断
        if(!CollectionUtils.isEmpty(attrValueList)){
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //设置平台属性id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                //保存
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }
    /**
     * 根据属性id查询属性对象
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //获取属性对象
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        //获取属性值集合
        List<BaseAttrValue> list = getAttrValueList(attrId);
        //设置属性值集合
        baseAttrInfo.setAttrValueList(list);
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoPage(SpuInfo spuInfo, Page<SpuInfo> infoPage) {
        // 创建对象
        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", spuInfo.getCategory3Id());
        return spuInfoMapper.selectPage(infoPage, queryWrapper);
    }

    /**
     * 根据属性id查询属性值集合
     * @param attrId
     * @return
     */
    private List<BaseAttrValue> getAttrValueList(Long attrId) {
        //创建条件对象
        QueryWrapper<BaseAttrValue> wrapper=new QueryWrapper<>();
        wrapper.eq("attr_id",attrId);
        //查询数据
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(wrapper);
        return baseAttrValueList;
    }
    /**
     *  获取销售属性
     * @return
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }
    /**
     * 保存spu
     * @param spuInfo
     *
     *  spuInfo涉及到的表
     * spu_info 基本信息表
     * spu_image 图片表
     * spu_poster 海报表
     * spu_sale_attr_value 销售属性值表
     * spu_sale_attr  销售属性表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存spu信息
        spuInfoMapper.insert(spuInfo);
        //保存图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //判断是否存在数据
        if(!CollectionUtils.isEmpty(spuImageList)){
            for (SpuImage spuImage : spuImageList) {
                //设置spuId
                spuImage.setSpuId(spuInfo.getId());
                //保存图片到数据库
                spuImageMapper.insert(spuImage);
            }
        }
        //保存海报
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        //判断
        if(!CollectionUtils.isEmpty(spuPosterList)){
            for (SpuPoster spuPoster : spuPosterList) {
                //设置spuId
                spuPoster.setSpuId(spuInfo.getId());
                //保存海报
                spuPosterMapper.insert(spuPoster);
            }
        }
        //保存销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //判断
        if(!CollectionUtils.isEmpty(spuSaleAttrList)){
            //保存销售属性
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                //设置spuId
                spuSaleAttr.setSpuId(spuInfo.getId());
                //保存
                spuSaleAttrMapper.insert(spuSaleAttr);
                //获取销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //判断
                if(!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    //保存销售属性值
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //设置spuId
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        //设置销售属性名称
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        //保存销售属性值
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    /**
     * 根据spuId查询销售属性和销售属性值集合
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        return  spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    /**
     * 根据spuId查询图片列表
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        //创建条件对象
        QueryWrapper<SpuImage> queryWrapper=new QueryWrapper<>();
        //select *from spu_image where spuid=spuId
        queryWrapper.eq("spu_id",spuId);
        return  spuImageMapper.selectList(queryWrapper);
    }

    /**
     * 保存skuInfo
     *  操作的表：
     *   sku_info 基本信息表
     *   sku_image sku图片表
     *   sku_sale_attr_value sku销售属性表
     *   sku_attr_value sku品台属性表
     *
     * @param skuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //设置is_sale
        skuInfo.setIsSale(0);
        //保存skuinfo
        skuInfoMapper.insert(skuInfo);
        //保存图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        //判断
        if(!CollectionUtils.isEmpty(skuImageList)){
            for (SkuImage skuImage : skuImageList) {
                //设置skuid
                skuImage.setSkuId(skuInfo.getId());
                //保存
                skuImageMapper.insert(skuImage);
            }
        }
        //保存平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //判断
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                //设置skuId
                skuAttrValue.setSkuId(skuInfo.getId());
                //保存
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
        //保存销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //判断
        if(!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //设置skuId
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                //设置spuId
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                //保存
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
    }

    /**
     * sku分页列表
     * @param skuInfoPage
     * @return
     */
    @Override
    public IPage<SkuInfo> skuListPage(Page<SkuInfo> skuInfoPage) {
        //排序
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(skuInfoPage, queryWrapper);
    }
    /**
     * 商品上架
     * 将is_sale改为1
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        //封装对象
        SkuInfo skuInfo=new SkuInfo();
        //设置条件
        skuInfo.setId(skuId);
        //设置修改的内容
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    /**
     * 商品的下架
     *  将is_sale改为0
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        //封装对象
        SkuInfo skuInfo=new SkuInfo();
        //设置条件
        skuInfo.setId(skuId);
        //设置修改的内容
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }
    /**
     * 根据skuId查询skuInfo信息和图片列表
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        //查询skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //根据skuId查询图片列表
        //查询条件对象
        //select *from sku_image where sku_id=skuId
        QueryWrapper<SkuImage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);
        //设置图片列表
        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }

    /**
     * 根据三级分类id获取分类信息
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 根据skuId查询sku实时价格
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //查询skuInfo对象
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //判断
        if(skuInfo != null){
            return skuInfo.getPrice();
        }
        return new BigDecimal("0");
    }
    /**
     * 根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrList ;
    }

    /**
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     * @return
     */
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        //查询对应关系集合
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuValueIdsMap(spuId);
        //创建map整合返回的数据
        Map<Object ,Object> resultMap = new HashMap();
        //判断
        if(!CollectionUtils.isEmpty(mapList)){
            for (Map map : mapList) {
                //map 只有一条数据
                //整合所有 key 属性id拼接的结果  value skuId
                resultMap.put(map.get("value_ids"), map.get("sku_id"));
            }
        }
        return resultMap;
    }

    /**
     * 根据spuid查询海报集合数据
     * @param spuId
     * @return
     */
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        //查询sql select *from spu_poster where  spu_id=spuId
        //创建条件对象
        QueryWrapper<SpuPoster> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        //执行查询
        List<SpuPoster> posterList = spuPosterMapper.selectList(queryWrapper);
        return posterList;
    }

    /**
     * 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectAttrList(skuId);
    }
}
