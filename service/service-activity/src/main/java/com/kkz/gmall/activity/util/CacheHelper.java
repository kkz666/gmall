package com.kkz.gmall.activity.util;

import java.util.concurrent.ConcurrentHashMap;

public class CacheHelper {
    public final static ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap();
    /**
     * 加入缓存
     *
     * @param key
     * @param cacheObject
     */
    public static void put(String key, Object cacheObject) {
        cacheMap.put(key, cacheObject);
    }
    /**
     * 获取缓存
     * @param key
     * @return
     */
    public static Object get(String key) {
        return cacheMap.get(key);
    }
    /**
     * 清除缓存
     *
     * @param key
     * @return
     */
    public static void remove(String key) {
        cacheMap.remove(key);
    }
    public static synchronized void removeAll() {
        cacheMap.clear();
    }
}
