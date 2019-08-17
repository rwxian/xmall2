package com.xmall.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName CookieUtil
 * @Description: Cookie工具类，向客户端发送cookie
 * @Author rwxian
 * @Date 2019/8/17 12:37
 * @Version V1.0
 **/
public class CookieUtil {

    private final static String COOKIE_DOMAIN = ".s-rwxian.cn";     // cookie作用域
    private final static String COOKIE_NAME = "xmall_login_token";  // cookie名字
    private final static Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    /**
     * @MethodName: writeLoginToken
     * @Description: 把cookie写入到客户端
     * @Param: [response, token]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/17 12:51
     */
    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");    // 代表设置在根目录

        // 如果这个maxage不设置的话，cookie不会写入硬盘，而是写入内存，只在当前页面有效.
        cookie.setMaxAge(60 * 60 * 24 *356);    // 单位是秒，如果是-1，代表永久
        logger.info("写入cookie cookieName{},cookieValue{} 到客户端！", cookie.getName(), cookie.getValue());
        response.addCookie(cookie);     // 写入cookie
    }

    /**
     * @MethodName: readLoginToken
     * @Description: 从客户端读取cookie
     * @Param: [request]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/17 12:59
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie ck : cookies) {
                logger.info("读取cookie：cookieName{},CookieValue{}!", ck.getName(), ck.getValue());
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    return ck.getValue();   // 返回登录cookie
                }
            }
        }
        return null;    // 没有读取到cookie
    }

    /**
     * @MethodName: deleteLoginToken
     * @Description: 删除客户端的登录cookie
     * @Param: [request, response]
     * @Return: void
     * @Author: rwxian
     * @Date: 2019/8/17 13:08
     */
    public static void deleteLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie ck : cookies) {
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0);    // 设置为0，代表删除cookie的意思
                    logger.info("删除cookie：cookieName{}, cookieValue{}!", ck.getName(), ck.getValue());
                    return;
                }
            }
        }
    }

}
