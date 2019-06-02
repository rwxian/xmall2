package com.xmall.service.impl;

import com.xmall.common.Const;
import com.xmall.common.ServerResponse;
import com.xmall.common.TokenCache;
import com.xmall.dao.UserMapper;
import com.xmall.pojo.User;
import com.xmall.service.IUserService;
import com.xmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/13 11:20
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int userCount = userMapper.checkUsername(username);
        if (userCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在！");
        }

        // 密码登录MD5,因为插入时密码时md5加密的，所以登录时也要加密，数据库对比的是加密后的数据
        String md5Pass = MD5Util.MD5EncodeUtf8(password);
        System.out.println("加密后的密码为:" + md5Pass);

        User user = userMapper.selectLogin(username, md5Pass);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    /**
     * 注册方法
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse validRespon = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validRespon.isSucess()) {      // 用户名已存在，结束执行，返回结果
            return validRespon;
        }

        validRespon = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validRespon.isSucess()) {      // 邮箱已存在，结束执行，返回结果
            return validRespon;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER); // 把用户角色设置为普通用户

        // MD5加密用户密码
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);  // 信息插入数据库
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败！");
        }
        return ServerResponse.createBySuccessMessage("注册成功！");
    }

    /**
     * 判断用户名,邮箱是否存在
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNoneBlank(type)) {    // 判断type是否为空，不包含空格，空格返回false
            // 开始校验
            if (Const.USERNAME.equals(type)) {  // type为用户名
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户已存在！");
                }
            }
            if (Const.EMAIL.equals(type)) {     // type为邮箱
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 查询找回密码对应的问题
     * @param username
     * @return
     */
    @Override
    public ServerResponse selectQuestion(String username) {
        ServerResponse validRespon = this.checkValid(username, Const.USERNAME);
        if (validRespon.isSucess()) {      // 用户名不存在，结束执行，返回结果
            return ServerResponse.createByErrorMessage("用户不存在！");
        }
        String question = userMapper.selectQuestionByUsername(username);    // 根据用户名查询问题
        if (StringUtils.isNoneBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createByErrorMessage("找回密码的问题是空的!");
    }

    /**
     * 验证问题答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            // 问题及问题答案与用户名对应，并且正确
            String forgetToken = UUID.randomUUID().toString();                     // 使用UUID生成Token
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);    // 调用TokenCache把Token放入到服务器缓存
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误！");
    }

    /**
     * 修改密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {                             // 判断页面传递过来的token
            return ServerResponse.createByErrorMessage("参数错误，token不能为空");
        }

        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSucess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);   // 从本地缓存中获取token
        if (StringUtils.isBlank(token)) {                                       // 判断缓存里的token是否为空
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        if (StringUtils.equals(forgetToken, token)) {                           // 判断页面传递token和服务器缓存中的token是否一致
            String md5Pwssword = MD5Util.MD5EncodeUtf8(passwordNew);            // 把密码进行md5加密后在比较
            int rowCount = userMapper.updatePasswordByUsername(username, md5Pwssword);  // 更新密码
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("密码修改成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("密码修改失败!");
    }

    /**
     * 登录状态下更新密码
     * @param passwordNew
     * @param passwordOld
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordNew, String passwordOld, User user) {
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());  // 查询密码是否正确
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);     // 更新密码
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功！");
        }

        return ServerResponse.createByErrorMessage("密码更新失败！");
    }

    /**
     * 更新个人信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse updateInformation(User user) {
        int reslutCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());       // 查询更改后的邮箱是否存在，不包含当前账户使用的邮箱
        if (reslutCount > 0) {
            return ServerResponse.createByErrorMessage("email已存在，请更换email在继续！");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);   // 更新个人信息
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功！", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败!");
    }

    /**
     * 获取当前登录用户信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);  // 根据id查找当前用户是否存在
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户！");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
