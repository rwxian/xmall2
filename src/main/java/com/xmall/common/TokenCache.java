package com.xmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/14 15:13
 * 使用Guava工具把Token保存到本地缓存中
 */
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);   //logback日志记录器

    public static final String TOKEN_PREFIX = "token_";     //token前缀

    //LRU(最少使用)算法设置缓存，初始容量1000，最大缓存10000，超过使用LRU算法，缓存有效期12小时
    private static LoadingCache<String, String> localCache =
            CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get取值时，如果key没有对应的值，就调用这个方法进行加载
                @Override
                public String load(String key) throws Exception {
                    return "null";      //防止getKey里面equals发生空指针异常
                }
            });

    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    //根据key获取value
    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                return null;
            }
            return value;
        } catch (Exception e) {
            logger.error("localCache get error!");
        }
        return null;
    }
}
