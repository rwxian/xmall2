package com.xmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.xmall.common.Const;
import com.xmall.common.ResponseCode;
import com.xmall.common.ServerResponse;
import com.xmall.pojo.User;
import com.xmall.service.IOrderService;
import com.xmall.util.CookieUtil;
import com.xmall.util.JsonUtil;
import com.xmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author rwxian@foxmail.com
 * @date 2019/5/9 12:26
 * 支付宝接入的Controller
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    // 订单部分

    /**
     * 创建订单
     * @param request
     * @param shippingId
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpServletRequest request, Integer shippingId) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        // TODO 优化登录验证代码
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.createOrder(user.getId(), shippingId);
    }

    /**
     * 取消订单
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpServletRequest request, Long orderNo) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.cancel(user.getId(), orderNo);
    }

    /**
     * 获取购物车商品信息
     * @param request
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpServletRequest request) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     * 订单详情
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpServletRequest request, Long orderNo) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }


    /**
     * 个人中心查看我的订单
     * @param request
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest request,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }


    // 支付部分

    /**
     * 支付功能
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(Long orderNo, HttpServletRequest request) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");   // 文件临时存储路径
        return iOrderService.pay(orderNo, user.getId(), path);
    }

    /**
     * 支付宝预下单成功后的回调处理接口，支付宝会把返回的结果放在request中
     * @param request
     * @return
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallBack(HttpServletRequest request) {
        HashMap<String, String> params = Maps.newHashMap();

        Map<String, String[]> requestParams = request.getParameterMap();    // 获取支付宝回调时传回的数据

        Set<String> paramsSet = requestParams.keySet();
        Iterator<String> iterator = paramsSet.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";    // 如果不是最后一个，就在后面拼接上逗号
            }
            params.put(name, valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

        // 非常重要，验证回调的正确性，是不是支付宝发的，同时还要避免重复通知
        params.remove("sign_type");     // 移除sign_type参数，验签不需要
        try {
            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            System.out.println("开始验签了！");

            if (!alipayRSACheckV2) {    // 验签不通过
                System.out.println("验签不通过！");
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，再恶意请求就报警找网警了！！！");
            }
            System.out.println("恭喜您，验签通过了！");
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常！", e);
        }
        // todo 验证各种数据，放在service

        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSucess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 前端轮询查询订单是否已经支付
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> alipayCallBack(HttpServletRequest request, Long orderNo) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);     // 根据请求获取登录时存入客户端Cookie的登录Token
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userString = RedisShardedPoolUtil.get(loginToken);          // 根据Token到Redis中读取登录用户的信息
        User user = JsonUtil.stringToObject(userString, User.class);// 使用反序列化工具把String转换为Json

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (serverResponse.isSucess()) {
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
