package com.kkz.gmall.product.service.impl;

import com.kkz.gmall.product.service.TestService;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    /**
     * 测试本地锁的局限性：
     *
     *  实现步骤：
     *     1.从redis中num数据
     *     2.对num数据进行+1
     *     3.设置到redis中
     *
     *
     *  压测工具 ab
     *
     *  ab  -n（一次发送的请求数）  -c（请求的并发数） 访问路径
     *  演示：
     *   ab -n 5000  -c 100 http://192.168.254.1
     *
     *    测试结果：5000次请求 ，结果未136
     *
     *synchronized的安全控制锁由jvm进程进行管理
     *
     * 测试集群情况下本地锁的问题：
     *  1. 搭建集群 8206 8216 8226
     *  2.ab -n 5000 -c 100 http://192.168.254.1/admin/product/test/testLock
     *
     *  结果 2490 ，有安全问题
     *
     * 解决思路：
     *         跨越jvm的锁机制实现
     *
     * 优化一：使用setnx的占坑思想
     *    1.获取数据前，先获取锁
     *    2.获取到了锁，才能操作
     *    3.操作完成后一定要释放锁
     *
     * 问题：
     *     setnx刚好获取到锁，业务逻辑出现异常，导致锁无法释放
     *
     * 解决：设置锁的超时问题
     *     代码实现：
     *         //1.从redis 中获取锁，setnx
     *         Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "111");
     *         //设置超时时间
     *         stringRedisTemplate.expire("lock",7, TimeUnit.SECONDS);
     *
     *     问题：当前先获取锁，在设置超时时间不是原子性操作。
     *
     *      2.6.12版本以后进行了增强：
     *
     *      EX: 设置超时时间秒级单位
     *      PX:  设置超时时间毫秒级单位
     *      NX:  类似于setnx的效果，当key不存在才可以操作
     *      XX:  仅当key存在时，才可以操作
     *Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "111",7,TimeUnit.SECONDS);
     *
     *  问题：
     *     设置锁的超时时间，如果程序的执行过慢，导致锁的早释放
     *
     *
     *  解决：
     *     UUID方案解决
     *
     *     实现思路：
     *      1.在获取锁的之前，获取一个uuid唯一值，目的是在获取锁时，添加标识
     *      2.在获取锁时，作为value存储
     *      3.在释放锁时，判断本地的uuid和存储到锁对应的值得uuid是否一直
     *
     *
     *  问题：uuid可以防止锁被误删，但是极端情况下仍然有误删除的可能
     *
     *  原因： 判断uuid和删除lock并非原子性操作
     *
     *  解决方案： 使用lua脚本实现原子操作
     *
     * Redisson 的使用步骤
     *   1.将RedissonClient对象注入到使用的类中
     *   2.获取锁
     *      Lock lock=redissonClient.getLock();
     *       lock.lock()
     *
     *   3.释放锁
     *    lock.unlock();
     *
     * 注意：在使用redisson时，可以不用自己实现自旋
     *
     */
    @Override
    public void testLock() {
        //模拟sku查询
        //定义key  sku:1314:info
        String lockSku = "sku:" + 1314 + ":info";
        //获取锁
        RLock lock = redissonClient.getLock(lockSku);
        //加锁
//        lock.lock();
        //加锁时，设置超时时间
//        lock.lock(7,TimeUnit.SECONDS);
        //加锁时，设置超时时间和等待时间
        try {
            boolean flag = lock.tryLock(100, 10, TimeUnit.SECONDS);
            //判断
            if(flag){
                //从redis 中查询num数据
                String value = stringRedisTemplate.opsForValue().get("num");
                //判断是否为空
                if(StringUtils.isBlank(value)){
                    return;
                }
                //对num数据加一处理
                int num = Integer.parseInt(value);
                //存储到redis
                stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
            }else{
                Thread.sleep(50);
                testLock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //释放锁
            lock.unlock();
        }
    }

//    @Override
//    public   void testLock() {
//
//        //0 ，生成uuid
//        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
//        //1.从redis 中获取锁，setnx
////        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "111");
//        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,7,TimeUnit.SECONDS);
//        //异常
////        //设置超时时间
////        stringRedisTemplate.expire("lock",7, TimeUnit.SECONDS);
//        if(lock){
//            //从redis 中查询num数据
//            String value = stringRedisTemplate.opsForValue().get("num");
//            //判断是否为空
//            if(StringUtils.isBlank(value)){
//                return;
//            }
//            //对num数据加一处理
//            int num = Integer.parseInt(value);
//            //存储到redis
//            stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
//            //定义lua脚本
//            String script="if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
//                    "then\n" +
//                    "    return redis.call(\"del\",KEYS[1])\n" +
//                    "else\n" +
//                    "    return 0\n" +
//                    "end";
//                //创建脚本对象
//            DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
//            //设置脚本
//            defaultRedisScript.setScriptText(script);
//            //设置返回值类型
//            defaultRedisScript.setResultType(Long.class);
//            //执行删除
//            stringRedisTemplate.execute(defaultRedisScript, Arrays.asList("lock"),uuid);
////            //获取当前的锁值
////            //判断
////            if(uuid.equals(stringRedisTemplate.opsForValue().get("lock"))){
////                //释放锁
////                stringRedisTemplate.delete("lock");
////            }
//        }else{
//            try {
//                //睡眠100
//                Thread.sleep(100);
//                //重试调用
//                testLock();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    /**
     * 本地锁的演示
     */
//    @Override
//    public  synchronized void testLock() {
//        //从redis 中查询num数据
//        String value = stringRedisTemplate.opsForValue().get("num");
//
//        //判断是否为空
//        if(StringUtils.isBlank(value)){
//
//            return;
//        }
//        //对num数据加一处理
//        int num = Integer.parseInt(value);
//        //存储到redis
//        stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
//    }


    /**
     * 读锁测试
     * @return
     */
    @Override
    public String readLock() {
        //初始化获取锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        //获取读锁
        RLock rLock = readWriteLock.readLock();
        //加锁
        rLock.lock(10, TimeUnit.SECONDS);
        //读取数据
        String msg = stringRedisTemplate.opsForValue().get("msg");
        //释放锁
//        rLock.unlock();
        return msg;
    }

    /**
     * 写锁测试
     * @return
     */
    @Override
    public String writeLock() {
        //初始化获取锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        //获取写锁
        RLock wLock = readWriteLock.writeLock();
        //加锁
//        rLock.lock();
        wLock.lock(10, TimeUnit.SECONDS);
//        boolean flag = wLock.tryLock(100, 10, TimeUnit.SECONDS);
        //写入到redis
        stringRedisTemplate.opsForValue().set("msg", String.valueOf(System.currentTimeMillis()));
        //释放锁
//        rLock.unlock();
        return "写入了一条数据到redis中";
    }
}
