package com.kkz.gmall.activity.service.impl;

import com.kkz.gmall.activity.mapper.SeckillGoodsMapper;
import com.kkz.gmall.activity.service.SeckillGoodsService;
import com.kkz.gmall.activity.util.CacheHelper;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.result.ResultCodeEnum;
import com.kkz.gmall.common.util.AuthContextHolder;
import com.kkz.gmall.common.util.DateUtil;
import com.kkz.gmall.common.util.MD5;
import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.model.activity.OrderRecode;
import com.kkz.gmall.model.activity.SeckillGoods;
import com.kkz.gmall.model.activity.UserRecode;
import com.kkz.gmall.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RabbitService rabbitService;
    /**
     * 查询秒杀列表
     * @return
     */
    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> values = redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS);
//         List<SeckillGoods> values= redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return values;
    }
    /**
     * 获取商品详情
     * @param skuId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        return (SeckillGoods) this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
    }

    /**
     * 获取抢单码
     * @param skuId
     * @param request
     * @return
     */
    @Override
    public Result getSeckillSkuIdStr(Long skuId, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        SeckillGoods seckillGoods = (SeckillGoods) this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
        //判断
        if (seckillGoods != null) {
            //获取当前时间
            Date curDate = new Date();
            //判断
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), curDate) && DateUtil.dateCompare(curDate, seckillGoods.getEndTime())) {
                //生成抢单码
                String encrypt = MD5.encrypt(userId);
                return Result.ok(encrypt);
            }
        }
        return Result.fail().message("获取抢单码失败！");
    }

    /**
     * 秒杀下单
     * @param skuId
     * @param request
     * @return
     */
    @Override
    public Result seckillOrder(Long skuId, HttpServletRequest request) {
        //校验下单码
        String skuIdStr = request.getParameter("skuIdStr");
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //校验状态位
        if (MD5.encrypt(userId).equals(skuIdStr)) {
            //校验状态  key :skuId 23 value :状态位 1或者0
            String status = (String) CacheHelper.get(skuId.toString());
            //判断
            // 状态位为null返回不合法
            if (StringUtils.isEmpty(status)) {
                return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
            }
            //判断状态值
            // 0表示卖完
            if ("0".equals(status)) {
                return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
            } else {
                //生成抢单信息对象
                UserRecode userRecode = new UserRecode();
                userRecode.setSkuId(skuId);
                userRecode.setUserId(userId);
                //发送消息排队
                this.rabbitService.sendMessage(
                        MqConst.EXCHANGE_DIRECT_SECKILL_USER,
                        MqConst.ROUTING_SECKILL_USER, userRecode
                );
                return Result.ok();
            }
        }
        return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
    }

    /**
     * 抢单
     * @param userId
     * @param skuId
     */
    @Override
    public void obtainOrder(String userId, Long skuId) {
        // 校验状态位
        String status = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(status)) {
            return;
        }
        // 校验是否下过单
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId.toString());
        if (!flag) {
            return;
        }
        //校验库存
        String stockSkuId = (String) this.redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        if(StringUtils.isEmpty(stockSkuId))  {
            this.redisTemplate.convertAndSend("seckillPush",skuId + ":0");
            return ;
        }
        //生成订单信息存储到redis
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setNum(1);
        SeckillGoods seckillGoods = this.getSeckillGoods(skuId);
        orderRecode.setSeckillGoods(seckillGoods);
        //订单码
        orderRecode.setOrderStr(MD5.encrypt(userId + skuId));
        //存储
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userId, orderRecode);
        //更新库存量
        this.updateStockCount(skuId);
    }
    /**
     * 修改mysql 修改 redis
     * @param skuId
     */
    private void updateStockCount(Long skuId) {
        //获取锁对象
        Lock lock = new ReentrantLock();
        //加锁
        lock.lock();
        try {
            //获取库存
            Long stockKey = this.redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
            //获取当前尚品的信息
            SeckillGoods seckillGoods = this.getSeckillGoods(skuId);
            //设置库存
            seckillGoods.setStockCount(stockKey.intValue());
            //修改数据库mysql
            this.seckillGoodsMapper.updateById(seckillGoods);
            //更新缓存
            this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId.toString(), seckillGoods);
        } finally {
            lock.unlock();
        }
    }
}
