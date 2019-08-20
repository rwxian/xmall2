package com.xmall.controller.ctrlcommon.interceptor;

import com.google.common.collect.Maps;
import com.xmall.common.Const;
import com.xmall.common.ServerResponse;
import com.xmall.pojo.User;
import com.xmall.util.CookieUtil;
import com.xmall.util.JsonUtil;
import com.xmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName AuthorityInterceptor
 * @Description: SpringMVC拦截器
 * @Author rwxian
 * @Date 2019/8/19 21:27
 * @Version V1.0
 **/
public class AuthorityInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);

    /**
     * @MethodName: preHandle
     * @Description: 在进入Controller前执行，做权限验证
     * @Param: [httpServletRequest, httpServletResponse, o]
     * @Return: boolean
     * @Author: rwxian
     * @Date: 2019/8/20 12:54
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handle)
            throws Exception {
        logger.info("后台请求拦截器：preHandle");
        HandlerMethod handlerMethod = (HandlerMethod) handle;

        // 解析handlerMethod
        String method = handlerMethod.getMethod().getName();        // 获取方法名
        String className = handlerMethod.getBean().getClass().getSimpleName();  // 获取类名

        // 解析参数，具体的key和value是什么，打印日志
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = httpServletRequest.getParameterMap();    // 获取前端请求时传递的参数
        Iterator iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String value = StringUtils.EMPTY;       // 给一个默认值为 ""
            Object obj = entry.getValue();          // 默认返回的是String数组
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                value = Arrays.toString(strs);      // 把参数转换为String字符串
            }
            requestParamBuffer.append(key).append("=").append(value);
        }

        // 如果是登录请求，就不拦截
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(method, "login")) {
            logger.info("拦截器遇到登录请求，直接通过-className:{},Method:{}!", className, method);
            return true;    // 登录请求，直接放过
        }

        logger.info("拦截器拦截非登录请求-className:{},Method:{},param:{}!", className, method, paramMap.toString());

        // 权限验证：1.判断用户是否登录，2.判断用户信息是否存在Redis中，3.判断用户是否为管理员
        User user = null;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isNotEmpty(loginToken)){     // 判断是否登录了
            // return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
            String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
            user = JsonUtil.stringToObject(userString, User.class);            // 使用反序列化工具把String转换为Json
        }

        if (user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)) { // 用户未登录或者不是管理员权限
            // 不在使用SpringMVC的返回，而是用response托管
            httpServletResponse.reset();                                            // 这里要添加reset，否则会报异常
            httpServletRequest.setCharacterEncoding("UTF-8");                       // 需设置编码，否则会乱码
            httpServletResponse.setContentType("application/json;charset=UTF-8");   // 返回json格式数据

            PrintWriter out = httpServletResponse.getWriter();
            if (user == null) {     // 如果时未登录用户

                // 如果是上传富文本请求，因为富文本插件要求只能返回Map，所以此处需要单独处理
                if (StringUtils.equals(className, "ProductManageController")
                        && StringUtils.equals(method, "richtextImgUpload")) {
                    logger.info("拦截器拦截-发生未登录的富文本上传操作-className:{},Method:{}!", className, method);
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("sucess", false);
                    resultMap.put("msg", "未登录，请登录管理员！");
                    out.print(JsonUtil.objectToString(resultMap));
                } else {
                    // 正常请求
                    out.print(JsonUtil.objectToString(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录！")));
                }
            } else {    // 用户不为空
                // 如果是上传富文本请求，因为富文本插件要求只能返回Map，所以此处需要单独处理
                if (StringUtils.equals(className, "ProductManageController")
                        && StringUtils.equals(method, "richtextImgUpload")) {
                    logger.info("拦截器拦截-发生不是管理员的富文本上传操作-className:{},Method:{}!", className, method);
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("sucess", false);
                    resultMap.put("msg", "无权限操作！");
                    out.print(JsonUtil.objectToString(resultMap));
                } else {
                    out.print(JsonUtil.objectToString(ServerResponse.createByErrorMessage("拦截器拦截，无操作权限！")));
                }
            }
            out.flush();
            out.close();
            return false;   // 不需要进入Controller了，直接返回
        }
        return true;        // 管理员已登录，进入Controller
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {

    }
}
