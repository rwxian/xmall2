package com.xmall.controller.backend;

import com.xmall.common.ServerResponse;
import com.xmall.service.ICategoryService;
import com.xmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/25 20:48
 * 添加分类和更新分类
 */

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加分类，需要验证用户是否是管理员
     *
     * @param request
     * @param categoryName
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpServletRequest request, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            // 是管理员
            // 增加处理分类的逻辑
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("无操作权限，需要管理员权限！");
        }*/

        // 拦截器已验证身份
        return iCategoryService.addCategory(categoryName, parentId);
    }

    /**
     * 更改分类名称
     * @param request
     * @param categoryId
     * @param categoryName
     * @return
     */

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpServletRequest request, Integer categoryId, String categoryName) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {     // 检查是否是管理员
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限！");
        }*/

        // 拦截器已验证身份
        return iCategoryService.updateCategoryName(categoryId, categoryName);
    }

    /**
     * 查询子节点分类信息
     * @param request
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpServletRequest request,
                                                      @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            // 查询字节点的分类信息，并且不递归，保持评级
            return iCategoryService.getChildrenParallerCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限！");
        }*/

        // 拦截器已验证身份
        // 查询子节点的分类信息，并且不递归，保持平级
        return iCategoryService.getChildrenParallerCategory(categoryId);
    }

    /**
     * 查询当前节点的id和递归字节点的id
     * @param request
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpServletRequest request,
                                                            @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            // 查询字节点的分类信息，并且不递归，保持平级
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限！");
        }*/

        // 拦截器已验证身份
        return iCategoryService.selectCategoryAndChildrenById(categoryId);
    }
}
