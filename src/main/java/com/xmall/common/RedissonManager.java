package com.xmall.common;

import com.xmall.util.PropertiesUtil;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * @ClassName RedissonManager
 * @Description: Ression初始化类
 * @Author rwxian
 * @Date 2019/8/22 11:08
 * @Version V1.0
 **/
@Component
public class RedissonManager {
    private Config config = new Config();
    private Redisson redisson = null;

    private static final Logger logger = LoggerFactory.getLogger(RedissonManager.class);

    public Redisson getRedisson() {
        return redisson;
    }

    private static String redis0Ip = PropertiesUtil.getProperty("redis0.ip");
    private static Integer redis0Port = Integer.parseInt(PropertiesUtil.getProperty("redis0.port"));
    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));

    /**
     * @MethodName: init
     * @Description: 初始化Redission，使用@PostConstruct注解，当构造器完成后会执行它，也可以用静态代码块
     * @Param: []
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/22 11:18
     */
    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(redis0Ip + ":" + redis0Port);
            redisson = (Redisson) Redisson.create(config);
            logger.info("Redisson初始化结束！");
        } catch (Exception e) {
            logger.error("redisson 初始化失败！",e);
        }
    }
}
