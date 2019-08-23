package com.xmall.common;

import com.xmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RedisShardedPool
 * @Description: Redis分布式连接池
 * @Author rwxian
 * @Date 2019/8/18 18:14
 * @Version V1.0
 **/
public class RedisShardedPool {
    // 设置为static是为了当Tomcat启动的时候Jedis连接池及就要加载进来
    private static ShardedJedisPool pool;  // Sharded jedis连接池

    // 最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));

    // 在jedispool中最大的Idle状态（空闲的）的jedis实例个数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));

    // 在jedispool中最小的Idle状态（空闲的）的jedis实例个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));

    // 在borrow一个jedis实例时，是否要进行验证操作，如果赋值为true,则拿到的jedis实例肯定时可以用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));

    // 在return一个jedis实例时，是否要进行验证操作，如果赋值为true,则放回jedispool的jedis实例肯定时可以用的
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    // 有几个Redis就写几个
    // redis0的ip地址
    private static String redis0Ip = PropertiesUtil.getProperty("redis0.ip");

    // redis0的端口
    private static Integer redis0Port = Integer.parseInt(PropertiesUtil.getProperty("redis0.port", "6379"));

    // redis0的密码
    private static String redis0Pass = PropertiesUtil.getProperty("redis0.pass");

    // redis1的ip地址
    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");

    // redis1的端口
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6380"));

    // redis1的密码
    private static String redis1Pass = PropertiesUtil.getProperty("redis1.pass");

    private static final Logger logger = LoggerFactory.getLogger(RedisShardedPool.class);

    /**
     * @MethodName: initPool
     * @Description: 初始化连接池
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/16 12:48
     */
    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setBlockWhenExhausted(true); // 连接耗尽时，是否阻塞，false会抛出异常，true阻塞直到超时，默认为true

        // pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);     // 实例化连接池
        JedisShardInfo info0 = new JedisShardInfo(redis0Ip, redis0Port, 1000 * 2);
        info0.setPassword(redis0Pass);     // 设置Redis密码
        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000 * 2);
        // info1.setPassword(redis1Pass);     // 设置Redis密码

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(1);   // Redis容器
        jedisShardInfoList.add(info0);
        // jedisShardInfoList.add(info1);

        /**
         * 构造参数含义分别为：配置；Redis容器；采用的hash算法，Hashing.MURMUR_HASH代表一致性算法；key的分布策略
         * Sharded 的每个Redis默认权重为1
         */
        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);   // 构建连接池
    }

    // jvm加载时就初始化连接池
    static {
        initPool();
    }

    /**
     * @MethodName: getJedis
     * @Description: 从连接池里拿一个分片的实例
     * @Param: []
     * @Return: redis.clients.jedis.Jedis
     * @Author: rwxian
     * @Date: 2019/8/16 12:51
     */
    public static ShardedJedis getJedis() {
        ShardedJedis resource = null;
        try {
            resource = pool.getResource();
        } catch (final Exception e) {
            logger.error("获取连接池时发生异常：{} Exception!!!", e.getMessage(), e);
            if (resource == null) {
                logger.error("Redis服务未启动!!!");
            }
        }
        return resource;
    }

    /**
     * @MethodName: returnResource
     * @Description: 把连接放回连接池
     * @Param: [jedis]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/16 12:53
     */
    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    /**
     * @MethodName: returnBrokenResource
     * @Description: 释放坏的连接到brokenResource，比如发生异常时的连接
     * @Param: [jedis]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/16 12:56
     */
    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);   // 底层已经判空
    }

    // 测试分片连接池
    /*public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for(int i =0;i<10;i++){
            jedis.set("key"+i,"value"+i);
        }
        String key0 = jedis.get("key0");
        System.out.println("key0:" + key0);

        String key2 = jedis.get("key9");
        System.out.println("key9:" + key2);
        returnResource(jedis);

        // pool.destroy();//临时调用，销毁连接池中的所有连接
        System.out.println("program is end");

    }*/
}


