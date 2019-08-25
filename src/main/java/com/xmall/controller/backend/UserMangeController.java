package com.xmall.controller.backend;

import com.xmall.common.Const;
import com.xmall.common.ServerResponse;
import com.xmall.pojo.User;
import com.xmall.service.IUserService;
import com.xmall.util.CookieUtil;
import com.xmall.util.JsonUtil;
import com.xmall.util.RedisShardedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/19 16:40
 */

@Controller
@RequestMapping("/manage/user")
public class UserMangeController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,
                                      HttpServletRequest request, HttpServletResponse response) {
        ServerResponse<User> loginResponse = iUserService.login(username, password);     // 用户登录
        if (loginResponse.isSucess()) {                         // 登录成功
            User user = loginResponse.getData();                // 获取登录用户
            if (user.getRole() == Const.Role.ROLE_ADMIN) {      // 判断用户是否为管理员
                // session.setAttribute(Const.CURRENT_USER, user);
                CookieUtil.readLoginToken(request);
                RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.objectToString(loginResponse.getData()),
                        Const.RedisCacheExtime.REDIS_CACHE_EXTIME);     // session报错到redis

                return loginResponse;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员，无法登录");
            }
        }
        return loginResponse;    // 如果没有登录成功，直接返回服务器响应对象
    }
}
