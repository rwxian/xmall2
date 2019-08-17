package com.xmall.util;

import com.xmall.common.RedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @ClassName RedisPoolUtil
 * @Description: Redis API封装与调试
 * @Author rwxian
 * @Date 2019/8/16 13:59
 * @Version V1.0
 **/
public class RedisPoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisPoolUtil.class);

    /**
     * @MethodName: set
     * @Description: 设置一个值
     * @Param: [key, value]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/16 14:23
     */
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result  = null;

        try {
            jedis = RedisPool.getJedis();   // 获取一个redis连接
            result = jedis.set(key, value); // 把数据插入到redis
        } catch (final Exception e) {
            logger.error("set key{}, value{} 时发生错误！", key, value, e);
            RedisPool.returnBrokenResource(jedis);  // 发生异常，把连接放入坏的池里面
        }
        RedisPool.returnResource(jedis);    // 释放连接
        return result;
    }

    /**
     * @MethodName: get
     * @Description: 根据key获取value
     * @Param: [key]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/16 14:24
     */
    public static String get(String key) {
        Jedis jedis = null;
        String result  = null;  // 执行结果

        try {
            jedis = RedisPool.getJedis();   // 获取一个redis连接
            result = jedis.get(key); // 把数据插入到redis
        } catch (final Exception e) {
            logger.error("get key{} 时发生错误！", key, e);
            RedisPool.returnBrokenResource(jedis);  // 发生异常，把连接放入坏的池里面
        }
        RedisPool.returnResource(jedis);    // 释放连接
        return result;
    }

    /**
     * @MethodName: del
     * @Description: 根据键删除
     * @Param: [key]
     * @Return: java.lang.Long
     * @Author: rwxian
     * @Date: 2019/8/16 14:45
     */
    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (final Exception e) {
            logger.error("del key{} 时出错！", key, e);
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * @MethodName: setEx
     * @Description: 向redis存入一个变量，同时设置过期时间（此项目中用于session服务部分）
     * @Param: [key, value, exTime:过期时间，单位是秒]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/16 14:28
     */
    public static String setEx(String key, String value, int exTime) {
        Jedis jedis = null;
        String result = null;  // 执行结果

        try {
            jedis = RedisPool.getJedis();   // 获取一个redis连接
            result = jedis.setex(key, exTime, value); // 把数据插入到redis
        } catch (final Exception e) {
            logger.error("set key{}, value{} 时发生错误！", key, value, e);
            RedisPool.returnBrokenResource(jedis);  // 发生异常，把连接放入坏的池里面
        }
        RedisPool.returnResource(jedis);    // 释放连接
        return result;
    }

    /**
     * @MethodName: expire
     * @Description: 重新设置键的过期时间，单位是秒
     * @Param: [key, exTime]
     * @Return: java.lang.Long
     * @Author: rwxian
     * @Date: 2019/8/16 14:39
     */
    public static Long expire(String key, int exTime) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (final Exception e) {
            logger.error("expire key{} 时出错！", key, e);
        }
        RedisPool.returnResource(jedis);
        return result;
    }
}
