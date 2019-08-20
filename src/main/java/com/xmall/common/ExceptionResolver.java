package com.xmall.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName ExceptionResolver
 * @Description: SpringMVC全局异常处理类，Component注解变为Spring组件bean
 * @Author rwxian
 * @Date 2019/8/19 20:34
 * @Version V1.0
 **/
@Component
public class ExceptionResolver implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                         Object o, Exception e) {
        logger.error("发生异常：{} Exception!", httpServletRequest.getRequestURI(), e);

        // 使用MappingJacksonJsonView对象把ModelAndView对象Json化
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());

        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "接口异常，详情请查看服务端日志！");
        modelAndView.addObject("data", e.toString());
        return modelAndView;
    }
}
