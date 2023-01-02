package com.kkz.gmall.common.cache;

import java.lang.annotation.*;

/**
 * @author atguigu-mqx
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {

    //  定义一个数据 sku:skuId
    //  目的用这个前缀要想组成 缓存的key！
    String prefix() default "cache:";

}
