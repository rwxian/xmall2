package com.xmall.task;

import com.xmall.common.Const;
import com.xmall.common.RedissonManager;
import com.xmall.service.IOrderService;
import com.xmall.util.PropertiesUtil;
import com.xmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName CloseOrderTask
 * @Description: TODO
 * @Author rwxian
 * @Date 2019/8/20 21:57
 * @Version V1.0
 **/
@Component
public class CloseOrderTask {

    private static final Logger logger = LoggerFactory.getLogger(CloseOrderTask.class);

    @Autowired
    private IOrderService iOrderService;

    @Autowired private RedissonManager redissonManager;

    /**
     * @MethodName: closeOrderTaskV1
     * @Description: 以当前时间为准，两个小时前下单，关闭未付款的订单，此方法仅支持单机
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/21 10:21
     */
    // @Scheduled(cron = "0 */1 * * * ?")      // 每一分钟（每一分钟的整数倍）执行一次
    public void closeOrderTaskV1() {
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);     // 关闭订单
    }

    /**
     * @MethodName: closeOrderTaskV2
     * @Description: 关闭超时未支付订单，有分布式锁，但是如果在执行的上锁操作后，tomcat重启或关闭了，那么还是会造成死锁。
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/21 15:38
     */
    // @Scheduled(cron = "0 */1 * * * ?")      // 每一分钟（每一分钟的整数倍）执行一次
    public void closeOrderTaskV2() {
        logger.info("关闭订单定时任务启动");
        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.time", "50000"));    // 分布式锁锁定时长
        // 把锁存入redis
        Long setNxResult = RedisShardedPoolUtil.setNx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeOut));      // 设置锁，value为当前时间戳+锁定时长
        if (setNxResult != null && setNxResult.intValue() == 1) {   // 如果返回1，说明锁设置成功
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            logger.info("没有获取分布式锁：{}！", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        logger.info("关闭订单定时任务结束");
    }

    /**
     * @MethodName: closeOrderTaskV3
     * @Description: 手动实现的Redis分布式锁定时关单，做到双重防死锁
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/21 18:54
     */
    // @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3() {
        logger.info("关闭订单定时任务启动");
        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.time", "5000"));    // 分布式锁锁定时长
        // 把锁存入redis，setNx具有原子性，如果锁已存在还未释放，则执行不成功
        Long setNxResult = RedisShardedPoolUtil.setNx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeOut));      // 设置锁，value为当前时间戳+锁定时长
        if (setNxResult != null && setNxResult.intValue() == 1) {
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

            // 如果tomcat进程是被kill掉的，未设置锁超时时间，可以通过比较时间戳来判断是否可以重置锁
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                        String.valueOf(System.currentTimeMillis() + lockTimeOut));  // 重置锁，同时返回旧的锁

                // 锁已经被释放 || 锁未释放，但是就是当前锁
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(getSetResult, lockValueStr))) {
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    logger.info("没有获取分布式锁：{}！", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                logger.info("没有获取分布式锁：{}！", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        logger.info("关闭订单定时任务结束");
    }

    /**
     * @MethodName: closeOrderTaskV4
     * @Description: Redisson实现的分布式锁，但不支持一致性算法，需要使用单独的Redis部署
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/22 11:34
     */
    // @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4() {
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK); // 通过Redisson实例获取Redisson锁
        boolean getLock = false;    // 是否获取锁
        try {
            //  让集群中的tomcat自己竞争锁，wait_time要设置未0，否则就可能出现两个tomcat同时获取到锁的情况，同时设置5秒后释放锁
            getLock = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (getLock) {
                logger.info("Redisson获取到分布式锁：{},ThreadName:{}!", Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time", "2"));
                iOrderService.closeOrder(hour);     // 关闭订单
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            logger.info("释放Redisson分布式锁！");
        }
    }

    /**
     * @MethodName: closeOrder
     * @Description: 竞争锁成功后，关闭订单
     * @Param: [lockName]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/22 11:35
     */
    private void closeOrder(String lockName) {
        RedisShardedPoolUtil.expire(lockName, 5);       // 设置锁的超时时间为5秒，5秒后自动销毁，防止死锁
        logger.info("获取锁：{},ThreadName:{}!", lockName, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time", "2"));
        iOrderService.closeOrder(hour);                         // 关闭超时未支付订单
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);   // 释放Redis中存入的锁
        logger.info("释放锁：{},ThredName:{}!", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
    }

    /**
     * @MethodName: delLocak
     * @Description: 在v2版本方法中锁支持分布式了，但如果在上锁后，还没有执行关单的方法之前，tomcat进程被kill掉，
     *               或者调用shutdown命令停止掉，此时由于没有设置redis中锁的过期时间，就会造成死锁。
     *               通过@PreDestory注解，当tomcat被关闭后，此注解作用的方法会执行，这样就可以执行删除锁的操作
     *               但是如果tomcat是被kill的，此方法不起作用
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/21 17:25
     */
    @PreDestroy
    public void delLocak() {
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);   // 释放Redis中存入的锁
    }

}
