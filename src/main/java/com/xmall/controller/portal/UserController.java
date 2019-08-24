package com.xmall.controller.portal;

import com.xmall.common.Const;
import com.xmall.common.ResponseCode;
import com.xmall.common.ServerResponse;
import com.xmall.pojo.User;
import com.xmall.service.IUserService;
import com.xmall.util.CookieUtil;
import com.xmall.util.JsonUtil;
import com.xmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,
                                      HttpServletResponse httpServletResponse) {

        ServerResponse<User> response = iUserService.login(username, password);     // 校验用户信息
        if (response.isSucess()) {      // 登录成功，把用户信息保存到session
            // session.setAttribute(Const.CURRENT_USER, response.getData());

            CookieUtil.writeLoginToken(httpServletResponse, session.getId());   // 设置cookie到客户端

            // 把session存入redis中,分别放入sessionId，登录的用户信息，session过期时间
            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.objectToString(response.getData()), Const.RedisCacheExtime.REDIS_CACHE_EXTIME);
        }
        return response;
    }

    /**
     * @MethodName: logout
     * @Description: 用户登出
     * @Param: [request]
     * @Return: com.xmall.common.ServerResponse<java.lang.String>
     * @Author: rwxian
     * @Date: 2019/8/17 18:16
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // session.removeAttribute(Const.CURRENT_USER);    // 清除用户对应的session信息
        String loginToken = CookieUtil.readLoginToken(request);     // 从请求中获取登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息！");
        }
        CookieUtil.deleteLoginToken(request, response);             // 删除登录Cookie
        Long del = RedisShardedPoolUtil.del(loginToken);                   // 删除Redis中存储的Token
        /*if (del == 1L) {
            return ServerResponse.createBySuccess();
        }*/
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 信息校验
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    /**
     * @MethodName: getUserInfo
     * @Description: 获取用户信息
     * @Param: [request]
     * @Return: com.xmall.common.ServerResponse<com.xmall.pojo.User>
     * @Author: rwxian
     * @Date: 2019/8/17 18:13
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);    // 使用反序列化工具把String转换为Json
        if (user != null) {         // 用户已经登录，把用户信息返回给页面
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息！");
    }

    /**
     * 查询找回密码对应的问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 使用本地缓存检查问题答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 忘记密码，找回密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        System.out.println("token是：" + forgetToken);
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登录状态下的密码更改
     * @param request
     * @param passwordNew
     * @param passwordOld
     * @return
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest request, String passwordNew, String passwordOld) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录！");
        }
        return iUserService.resetPassword(passwordNew, passwordOld, user);
    }

    /**
     * 登录状态下进行信息更新
     * @param request
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpServletRequest request, User user) {
        // User currentUser = (User) session.getAttribute(Const.CURRENT_USER);     // 从session中获取当前登录用户
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User currentUser = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json，为登录用户
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录!");
        }

        user.setId(currentUser.getId());    // 为防止横向越权问题，把用户id设置成当前用户的id
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> serverResponse = iUserService.updateInformation(user);  // 更新信息
        if (serverResponse.isSucess()) {    // 更新成功
            serverResponse.getData().setUsername(user.getUsername());        // 设置用户名
            // session.setAttribute(Const.CURRENT_USER, serverResponse.getData());     // 把更新后的信息存入session中
            RedisShardedPoolUtil.setEx(loginToken, JsonUtil.objectToString(serverResponse.getData()), Const.RedisCacheExtime.REDIS_CACHE_EXTIME);
        }
        return serverResponse;
    }

    /**
     * 获取用户详细信息
     * @param request
     * @return
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpServletRequest request) {
        // User currentUser = (User) session.getAttribute(Const.CURRENT_USER); // 从session中获取当前登录用户
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User currentUser = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json，为登录用户
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登录!status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
