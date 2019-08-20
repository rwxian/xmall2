package com.xmall.controller.ctrlcommon;

import com.xmall.common.Const;
import com.xmall.pojo.User;
import com.xmall.util.CookieUtil;
import com.xmall.util.JsonUtil;
import com.xmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @ClassName SessionExpireFilter
 * @Description: 过滤器，用于重置session过期时间
 * @Author rwxian
 * @Date 2019/8/17 22:25
 * @Version V1.0
 **/
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * @MethodName: doFilter
     * @Description: 每次操作都重置session时间为30分钟
     * @Param: [servletRequest, servletResponse, filterChain]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/17 22:33
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String loginToken = CookieUtil.readLoginToken(request);     // 获取登录token
        if (StringUtils.isNotEmpty(loginToken)) {
            // 如果token不没空，继续获取User信息
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            User user = JsonUtil.stringToObject(userJsonStr, User.class);
            if (user != null) {
                // 如果user不为空，则重置session时间
                RedisShardedPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_CACHE_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
