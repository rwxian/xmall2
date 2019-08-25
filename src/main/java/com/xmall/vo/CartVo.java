package com.xmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author rwxian@foxmail.com
 * @date 2019/5/2 16:55
 * 购物车整体信息实体类
 */
public class CartVo {

    private List<CartProductVo> cartProductVoList;  // 购物车中的商品
    private BigDecimal cartTotalPrice; // 购物车所以商品的总价
    private Boolean allChecked; // 是否已经都勾选
    private String imageHost;   // 购物车显示的图片的前缀

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
