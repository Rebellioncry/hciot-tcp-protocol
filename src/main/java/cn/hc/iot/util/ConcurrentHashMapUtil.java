package cn.hc.iot.util;

import io.vertx.core.net.NetSocket;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapUtil {
    private static ConcurrentHashMap<String, NetSocket> cacheMap = new ConcurrentHashMap<>();

    /**
     * 获取缓存的对象
     * @param key
     * @return value
     */
    public static NetSocket getCache(String key) {
        // 如果缓冲中有该账号，则返回value
        if (cacheMap.containsKey(key)) {
            return cacheMap.get(key);
        }
        return null;
    }


    /**
     * 初始化缓存
     * @param key
     */
    public static void putCache(String key,NetSocket value) {
        // 一般是进行数据库查询，将查询的结果进行缓存
        cacheMap.putIfAbsent(key, value);
    }


    /**
     * 拼接一个缓存key
     * @param key
     */
    private static String getCacheKey(String key) {
        return Thread.currentThread().getId() + "-" + key;
    }


    /**
     * 移除缓存信息
     * @param key
     */
    public static void removeCache(String key) {
        cacheMap.remove(key);
    }
}
