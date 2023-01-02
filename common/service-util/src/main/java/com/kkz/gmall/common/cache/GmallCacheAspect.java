package com.kkz.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.kkz.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author atguigu-mqx
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //  定义一个环绕通知！
    @SneakyThrows
    @Around("@annotation(com.kkz.gmall.common.cache.GmallCache)")
    public Object gmallCacheAspectMethod(ProceedingJoinPoint point){
        //  定义一个对象
        Object obj = new Object();
        /*
         业务逻辑！
         1. 必须先知道这个注解在哪些方法 || 必须要获取到方法上的注解
         2. 获取到注解上的前缀
         3. 必须要组成一个缓存的key！
         4. 可以通过这个key 获取缓存的数据
            true:
                直接返回！
            false:
                分布式锁业务逻辑！
         */
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        //   获取到注解上的前缀
        String prefix = gmallCache.prefix();
        //  组成缓存的key！ 获取方法传递的参数
        String key = prefix+ Arrays.asList(point.getArgs()).toString();
        try {
            //  可以通过这个key 获取缓存的数据
            obj = this.getRedisData(key,methodSignature);
            if (obj==null){
                //  分布式业务逻辑
                //  设置分布式锁，进入数据库进行查询数据！
                RLock lock = redissonClient.getLock(key + ":lock");
                //  调用trylock方法
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //  判断
                if(result){
                    try {
                        //  执行业务逻辑：直接从数据库获取数据
                        //  这个注解 @GmallCache 有可能在 BaseCategoryView getCategoryName , List<SpuSaleAttr> getSpuSaleAttrListById ....
                        obj = point.proceed(point.getArgs());
                        //  防止缓存穿透
                        if (obj==null){
                            Object object = new Object();
                            //  将缓存的数据变为 Json 的 字符串
                            this.redisTemplate.opsForValue().set(key, JSON.toJSONString(object),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return object;
                        }
                        //  将缓存的数据变为 Json 的 字符串
                        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(obj),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return obj;
                    }finally {
                        //  解锁
                        lock.unlock();
                    }
                }else {
                    //  没有获取到
                    try {
                        Thread.sleep(100);
                        return gmallCacheAspectMethod(point);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                //  直接从缓存获取的数据！
                return obj;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //  数据库兜底！
        return point.proceed(point.getArgs());
    }

    /**
     * 从缓存中获取数据！
     * @param key
     * @return
     */
    private Object getRedisData(String key,MethodSignature methodSignature) {
        //  在向缓存存储数据的时候，将数据变为Json 字符串了！
        //  通过这个key 获取到缓存的value
        String strJson = (String) this.redisTemplate.opsForValue().get(key);
        //  判断
        if(!StringUtils.isEmpty(strJson)){
            //  将字符串转换为对应的数据类型！
            return JSON.parseObject(strJson,methodSignature.getReturnType());
        }
        return null;
    }

}
