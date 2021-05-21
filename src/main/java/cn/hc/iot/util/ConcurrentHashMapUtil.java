package cn.hc.iot.util;

import io.vertx.core.net.NetSocket;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapUtil {
    private static ConcurrentHashMap<NetSocket,String> cacheMap = new ConcurrentHashMap<>();

    /**
     * 获取缓存的对象
     * @param key
     * @return value
     */
    public static String getCache(NetSocket key) {
        // 如果缓冲中有该账号，则返回value
        if (cacheMap.equals(key)) {
            return cacheMap.get(key);
        }
        return "";
    }


    /**
     * 初始化缓存
     * @param key
     */
    public static void putCache(NetSocket key,String value) {
        cacheMap.putIfAbsent(key, value);
    }


    /**
     * 移除缓存信息
     * @param key
     */
    public static void removeCache(NetSocket key) {
        cacheMap.remove(key);
    }


    public static ConcurrentHashMap<NetSocket, String> all(){
        return cacheMap;
    }
}
