package com.xmall.util;

import com.xmall.common.RedisShardedPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;

/**
 * @ClassName RedisShardedPoolUtil
 * @Description: TODO
 * @Author rwxian
 * @Date 2019/8/18 20:15
 * @Version V1.0
 **/
public class RedisShardedPoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisShardedPoolUtil.class);

    /**
     * @MethodName: set
     * @Description: 设置一个值
     * @Param: [key, value]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/16 14:23
     */
    public static String set(String key, String value) {
        ShardedJedis jedis = null;
        String result  = null;

        try {
            jedis = RedisShardedPool.getJedis();   // 获取一个redis连接
            result = jedis.set(key, value); // 把数据插入到redis
        } catch (final Exception e) {
            logger.error("set key:{}, value:{} 时发生错误！", key, value, e);
            RedisShardedPool.returnBrokenResource(jedis);  // 发生异常，把连接放入坏的池里面
            return result;
        }
        RedisShardedPool.returnResource(jedis);    // 释放连接
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
        ShardedJedis jedis = null;
        String result  = null;  // 执行结果

        try {
            jedis = RedisShardedPool.getJedis();   // 获取一个redis连接
            result = jedis.get(key); // 根据键获取值
            logger.info("根据--key:{}--从Redis中获取对应的值", key);
        } catch (final Exception e) {
            logger.error("get key:{} 时发生错误！", key, e);
            RedisShardedPool.returnBrokenResource(jedis);  // 发生异常，把连接放入坏的池里面
            return result;
        }
        RedisShardedPool.returnResource(jedis);    // 释放连接
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
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
            logger.info("删除Redis中存储的--key:{}--", key);
        } catch (final Exception e) {
            logger.error("del key:{} 时出错！", key, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
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
        ShardedJedis jedis = null;
        String result = null;  // 执行结果

        try {
            jedis = RedisShardedPool.getJedis();   // 获取一个redis连接
            result = jedis.setex(key, exTime, value); // 把数据插入到redis
            logger.info("向Redis存入值--key:{}, value:{}, exTime:{}--!", key, value, exTime);
        } catch (final Exception e) {
            logger.error("set-- key:{}, value:{} --时发生错误！", key, value, e);
            RedisShardedPool.returnBrokenResource(jedis);  // 发生异常，把连接放入坏的池里面
            return result;
        }
        RedisShardedPool.returnResource(jedis);    // 释放连接
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
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key, exTime);
            logger.info("向Redis设置--key:{}--的过期时间为--exTime:{}秒-- !", key, exTime);
        } catch (final Exception e) {
            logger.error("expire key:{} 时出错！", key, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);    // 释放连接
        return result;
    }

    public static void main(String[] args) {
        ShardedJedis jedis = RedisShardedPool.getJedis();

        RedisPoolUtil.set("keyTest","value");

        String value = RedisPoolUtil.get("keyTest");

        RedisPoolUtil.setEx("keyex","valueex",60*10);

        RedisPoolUtil.expire("keyTest",60*20);

        RedisPoolUtil.del("keyTest");


        String aaa = RedisPoolUtil.get(null);
        System.out.println(aaa);

        System.out.println("end");
    }
}
