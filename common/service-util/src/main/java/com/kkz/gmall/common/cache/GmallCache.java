package com.kkz.gmall.common.cache;

import java.lang.annotation.*;

/**
 * 元注解： 简单理解就是修饰注解的注解
 *
 * @Target: 用于描述注解的使用范围，简单理解就是当前注解可以用在什么地方
 *@Retention: 表示注解的声明周期
 *             SOURCE:之存在类文件中，源码中，在class字节码不存在
 *             CLASS：存在到字节码文件中
 *             RUNTIME：运行时
 * @Inherited: 继承 表示被GmallCache修饰的类的子类会不会继承GmallCache
 *
 * @Documented: 文档 javadoc指令可以生成文档
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {

    //  定义一个数据 sku:skuId
    //  目的用这个前缀要想组成 缓存的key！
    //缓存的前缀
    String prefix() default "cache:";

    //缓存的后缀
    String suffix() default  ":info";

}
