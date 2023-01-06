package com.kkz.gmall.product;

import com.kkz.gmall.common.constant.RedisConst;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.kkz.gmall")
public class ServiceProductApplication implements CommandLineRunner {
    @Autowired
    private RedissonClient redissonClient;
    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class, args);
    }
    /**
     * 初始化布隆过滤器
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        //获取布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //初始化布隆过滤器  计算元素的数量  比如预计有多少个sku
        bloomFilter.tryInit(1000L,0.001);
    }
}
