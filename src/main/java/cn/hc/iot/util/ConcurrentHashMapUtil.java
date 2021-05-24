package cn.hc.iot.util;

import io.vertx.core.net.NetSocket;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapUtil {
    private static ConcurrentHashMap<String,NetSocket> cacheMap = new ConcurrentHashMap<>();

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
        cacheMap.putIfAbsent(key, value);
    }


    /**
     * 移除缓存信息
     * @param key
     */
    public static void removeCache(String key) {
        cacheMap.remove(key);
    }


    public static ConcurrentHashMap<String, NetSocket> all(){
        return cacheMap;
    }
}
