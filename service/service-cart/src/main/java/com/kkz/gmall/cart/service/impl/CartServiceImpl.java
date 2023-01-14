package com.kkz.gmall.cart.service.impl;

import com.kkz.gmall.cart.service.CartService;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.util.AuthContextHolder;
import com.kkz.gmall.common.util.DateUtil;
import com.kkz.gmall.model.cart.CartInfo;
import com.kkz.gmall.model.product.SkuInfo;
import com.kkz.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 加入购物车
     * @param skuId
     * @param skuNum
     *
     *  存储数据：
     *    1.区别用户
     *    2.区别商品
     *
     *    Hash
     *
     *    key:用户id
     *    filed: 商品id
     *    vlaue:商品对象信息
     *
     *  user:1:cart   22    CartInfo
     *
     *  指令：HSET key field value
     *
     * 思路：
     *      1.先根据用户id获取购物车列表
     *      2.判读是否存在当前添加的商品
     *       存在：数量相加
     *       不存在： 新建添加
     *
     *
     */
    @Override
    public void addToCart(Long skuId, Integer skuNum, HttpServletRequest request) {
        // 获取用户id
        String userId = AuthContextHolder.getUserId(request);
        // 判断
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        //定义key
        // user:{userId}:cart
        String cartKey = this.getKey(userId);
        //获取购物车列表 String == user:1:cart = ,String == 22==,  CartInfo
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        //定义购车详情信息对象
        CartInfo cartInfo = null;
        //判断当前列表是否包含sku
        if (boundHashOps.hasKey(skuId.toString())) {
            //存在 数量相加
            cartInfo = boundHashOps.get(skuId.toString());
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfo.setUpdateTime(new Date());
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
        } else {
            //不存在
            cartInfo = new CartInfo();
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            //远程请求sku详情数据
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
            //实时价格
            cartInfo.setSkuPrice(skuInfo.getPrice());
        }
        // 存储
        boundHashOps.put(skuId.toString(), cartInfo);
    }
    /**
     * 展示购物车--合并购物车
     *  1.未登录的购物车
     *  2.登录后的购物车-合并
     */
    @Override
    public List<CartInfo> cartList(HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //获取用户临时id
        String userTempId = AuthContextHolder.getUserTempId(request);
        // 定义未登录购物车
        List<CartInfo> nologinCartList = null;
        // 获取未登录数据
        if(!StringUtils.isEmpty(userTempId)){
            nologinCartList = redisTemplate.boundHashOps(this.getKey(userTempId)).values();
        }
        // 表示未登录
        if (StringUtils.isEmpty(userId) && !CollectionUtils.isEmpty(nologinCartList)) {
            nologinCartList.sort((o1, o2) -> {
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
            });
            return nologinCartList;
        }
        // 登录后的合并
        List<CartInfo> loginCartList = null;
        // 获取登录后的数据
        BoundHashOperations<String,String,CartInfo> boundHashOps = redisTemplate.boundHashOps(this.getKey(userId));
        // 判断是否为空
        if (!CollectionUtils.isEmpty(nologinCartList)) {
            // 合并
            nologinCartList.forEach(cartInfo -> {
                // 判断登录后的集合中是否包含
                if (boundHashOps.hasKey(cartInfo.getSkuId().toString())) {
                    // 已经在登录的购物车列表中存在 --数量更改
                    CartInfo loginCartInfo = boundHashOps.get(cartInfo.getSkuId().toString());
                    // 更新数量
                    loginCartInfo.setSkuNum(loginCartInfo.getSkuNum() + cartInfo.getSkuNum());
                    // 更新时间
                    loginCartInfo.setUpdateTime(new Date());
                    // 选中状态
                    if (cartInfo.getIsChecked().intValue() == 1) {
                        loginCartInfo.setIsChecked(1);
                    }
                    boundHashOps.put(cartInfo.getSkuId().toString(), loginCartInfo);
                } else {
                    // 添加到登录后的购物车列表
                    // 添加userId
                    cartInfo.setUserId(userId);
                    // 更新时间
                    cartInfo.setUpdateTime(new Date());
                    boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
                }
            });
            // 未登录的购物车数据--清除
            redisTemplate.delete(getKey(userTempId));
        }
        // 获取登录后的购物车列表
        loginCartList = boundHashOps.values();
        if (CollectionUtils.isEmpty(loginCartList)) {
            loginCartList = new ArrayList<>();
        }
        // 排序
        if (!CollectionUtils.isEmpty(loginCartList)) {
            loginCartList.sort((o1, o2)->{
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
            });
        }
        return loginCartList;
    }
    /**
     * 获取操作购物车的key
     * @param userId
     * @return
     * user:用户id:cart
     */
    private String getKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
