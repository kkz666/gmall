package com.kkz.gmall.activity.receiver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkz.gmall.activity.mapper.SeckillGoodsMapper;
import com.kkz.gmall.activity.service.SeckillGoodsService;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.util.DateUtil;
import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.model.activity.SeckillGoods;
import com.kkz.gmall.model.activity.UserRecode;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Component
public class SeckillReceiver {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 抢单消费
     * @param userRecode
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER, autoDelete = "false"),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    @SneakyThrows
    public void seckillOrder(UserRecode userRecode, Message message, Channel channel) {
        try {
            //判断
            if (userRecode != null) {
                // 抢单
                this.seckillGoodsService.obtainOrder(userRecode.getUserId(), userRecode.getSkuId());
            }
        } catch (Exception e) {
            //处理问题
            e.printStackTrace();
        }
        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 导入商品到redis预热
     *
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK, autoDelete = "false"),
            key = {MqConst.ROUTING_TASK_1}
    ))
    @SneakyThrows
    public void importToRedis(Message message, Channel channel) {
        //查询mysql中符合条件的数据  时间 状态 库存
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        //添加时间条件  2022-07-22
        queryWrapper.eq("date_format(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        //添加状态
        queryWrapper.eq("status", "1");
        //库存
        queryWrapper.gt("stock_count", 0);

        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
        //判断
        if (!CollectionUtils.isEmpty(seckillGoodsList)) {
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //判断缓存中是否存在该商品
                Boolean flag = this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).hasKey(seckillGoods.getSkuId().toString());
                if (flag) {
                    //跳过本次循环
                    continue;
                }
                //存储数据预热
                this.redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, seckillGoods.getSkuId().toString(), seckillGoods);
                //遍历库存
                for (Integer i = 0; i < seckillGoods.getStockCount(); i++) {
                    //防止库存超卖-存储到redis
                    this.redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId(), seckillGoods.getSkuId().toString());
                }
                //发布状态位-在内存中添加一个当前商品可抢购的标识
                this.redisTemplate.convertAndSend("seckillPush", seckillGoods.getSkuId() + ":1");
            }
        }
        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
