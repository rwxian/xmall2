package com.xmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author rwxian@foxmail.com
 * @date 2019/5/12 11:04
 */
public class OrderProductVo {
    private List<OrderItemVo> orderItemVoList;
    private BigDecimal productTotalPrice;
    private String imageHost;

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    @Override
    public String toString() {
        return "OrderProductVo{" +
                "orderItemVoList=" + orderItemVoList +
                ", productTotalPrice=" + productTotalPrice +
                ", imageHost='" + imageHost + '\'' +
                '}';
    }
}
