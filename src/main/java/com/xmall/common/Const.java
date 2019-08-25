package com.xmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/13 14:29
 * 常量类
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";    // 当前用户
    public static final String EMAIL = "email";                 // 邮箱
    public static final String USERNAME = "username";

    public interface Role {
        int ROLE_CUSTOMER = 0;  //普通用户
        int ROLE_ADMIN = 1;     //管理员
    }

    /**
     * 商品的排序
     */
    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_asc", "price_desc");
    }

    public interface Cart {
        int CHECKED = 1;    // 购物车中商品选中状态
        int UN_CHECKED = 0; // 购物车中商品未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";       // 限制失败
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS"; // 限制成功
    }

    /**
     * 商品状态枚举类
     */
    public enum ProductStatusEnum {
        ON_SALE(1, "在线！");
        //ON_SALE(1, "在线！");
        private String value;
        private int code;

        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * 订单状态枚举
     */
    public enum OrderStatusEnum {
        CANCELED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已付款"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        private String value;
        private int code;

        OrderStatusEnum(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum orderStatusEnum : values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举！");
        }
    }

    /**
     * 支付宝回调时的状态
     */
    public interface AlipayCallback {
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";  // 等待买家付款
        String TRADE_STATUS_TRADE_SUCESS = "TRADE_SUCESS";  // 付款成功

        String RESPONSE_SUCCESS = "success";    // 付款成功
        String RESPONSE_FAILED = "failed";      // 付款失败
    }

    /**
     * 支付平台，为后期接入微信支付做准备
     */
    public enum PayPlatformEnum {
        ALIPAY(1, "支付宝");

        private String value;
        private int code;

        PayPlatformEnum(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    /**
     * 支付方式，为后期扩展做准备
     */
    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付！");
        private String value;
        private int code;

        PaymentTypeEnum(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举！");
        }
    }
}
