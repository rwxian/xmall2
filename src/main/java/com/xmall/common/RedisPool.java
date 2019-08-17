package com.xmall.common;

import com.xmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @ClassName RedisPool
 * @Description: Redis连接池类
 * @Author rwxian
 * @Date 2019/8/16 11:33
 * @Version V1.0
 **/
public class RedisPool {

    // 设置为static是为了当Tomcat启动的时候Jedis连接池及就要加载进来
    private static JedisPool pool;  // jedis连接池

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

    // redis的ip地址
    private static String redisIp = PropertiesUtil.getProperty("redis.ip");

    // redis的端口
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port", "6379"));

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

        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);     // 实例化连接池
    }

    // jvm加载时就初始化连接池
    static {
        initPool();
    }

    /**
     * @MethodName: getJedis
     * @Description: 从连接池里拿一个实例
     * @Param: []
     * @Return: redis.clients.jedis.Jedis
     * @Author: rwxian
     * @Date: 2019/8/16 12:51
     */
    public static Jedis getJedis() {
        return pool.getResource();
    }

    /**
     * @MethodName: returnResource
     * @Description: 把连接放回连接池
     * @Param: [jedis]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/16 12:53
     */
    public static void returnResource(Jedis jedis) {
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
    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);   // 底层已经判空
    }

    //测试一下
    /*public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("test", "testvalue");
        returnResource(jedis);

        pool.destroy();     // 测试时临时销毁连接池，正式使用不要做
        System.out.println("-----------程序执行成功----------");
    }*/
}
