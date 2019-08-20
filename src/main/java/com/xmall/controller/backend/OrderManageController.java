package com.xmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.xmall.common.ServerResponse;
import com.xmall.service.IOrderService;
import com.xmall.service.IUserService;
import com.xmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author rwxian@foxmail.com
 * @date 2019/5/12 13:10
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    /**
     * 后台查看所有订单
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员！");
        }

        if (iUserService.checkAdminRole(user).isSucess()) {
            return iOrderService.manageList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }*/

        // 拦截器已验证身份
        return iOrderService.manageList(pageNum, pageSize);
    }

    /**
     * 后台查看订单明细
     * @param orderNo
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(Long orderNo) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员！");
        }

        if (iUserService.checkAdminRole(user).isSucess()) {
            return iOrderService.manageDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }*/

        // 拦截器已验证身份
        return iOrderService.manageDetail(orderNo);
    }

    /**
     * 后台根据订单号进行搜索，后期扩展为模糊查询
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(Long orderNo,
                                                @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员！");
        }

        if (iUserService.checkAdminRole(user).isSucess()) {
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }*/

        // 拦截器已验证身份
        return iOrderService.manageSearch(orderNo, pageNum, pageSize);
    }

    /**
     * 后台发货
     * @param orderNo
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSearch(Long orderNo) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        /*String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员！");
        }

        if (iUserService.checkAdminRole(user).isSucess()) {
            return iOrderService.manageSendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }*/

        // 拦截器已验证身份
        return iOrderService.manageSendGoods(orderNo);
    }
}
