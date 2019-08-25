package com.xmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.xmall.common.Const;
import com.xmall.common.ResponseCode;
import com.xmall.common.ServerResponse;
import com.xmall.dao.CartMapper;
import com.xmall.dao.ProductMapper;
import com.xmall.pojo.Cart;
import com.xmall.pojo.Product;
import com.xmall.service.ICartService;
import com.xmall.util.BigDecimalUtil;
import com.xmall.util.PropertiesUtil;
import com.xmall.vo.CartProductVo;
import com.xmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author rwxian@foxmail.com
 * @date 2019/5/2 15:56
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品到购物车
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null) { // 此商品不在购物车，需要新增此产品的记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);    // 设置数量
            cartItem.setChecked(Const.Cart.CHECKED);    // 默认为选中状态
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        } else {
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    /**
     * 更新购物车
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误！");
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
            int i = cartMapper.updateByPrimaryKeySelective(cart);
        }

        return this.list(userId);
    }

    /**
     * 删除购物车商品
     * @param userId
     * @param productIds
     * @return
     */
    @Override
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if (productIdList == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        cartMapper.deleteByUserIdProductId(userId, productIdList);  // 删除产品
        return this.list(userId);
    }

    /**
     * 查询当前用户的购物车商品
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);    // 从db中获取最新信息
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 全选或反选的方法
     * @param userId
     * @param checked
     * @return
     */
    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    /**
     * 获取当前用户购物车的商品总数量
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 购物车信息，核心方法。添加、更新、删除都会调用此方法，它会在db中做各种校验，然后重新计算，保证数据的正确性
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);    // 查询当前用户购物车中的所有商品
        List<CartProductVo> cartProductVoList = Lists.newArrayList();   // 初始化一个List集合
        BigDecimal cartTotalPrice = new BigDecimal("0");            // 用于价格计算

        if (CollectionUtils.isNotEmpty(cartList)) {     // 如果当前用户的购物车不为空
            for (Cart cartItem : cartList) {            // 遍历购物车，并把每种商品的信息封装为一个cartProductVo对象
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());    // 获取商品信息
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    // 判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) { // 如果添加的此商品数量已经超过了此商品的总库存数
                        buyLimitCount = cartItem.getQuantity();     // 购物车中当前商品的数量最多只能为此商品的总库存数
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        // 更新购物车中此商品的数量
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);

                    // 计算当前商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mult(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());  // 勾选产品
                }

                // 整个购物车的总价
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    // 如果已经勾选，增加到整个的购物车总价中
                    System.out.println("------------cartTotalPrice:"+cartTotalPrice.doubleValue());
                    System.out.println("------------cartProductVo.getProductTotalPrice():"+cartProductVo.getProductTotalPrice().doubleValue());
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);   // 把购物车中的所有商品添加到集合中
            }
        }

        // 把购物车信息封装为CartVo对象
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId)); // 判断购物车的商品是否是全选
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix")); // 添加购物车中图片链接的前缀

        return cartVo;
    }

    /**
     * 判断购物车的商品是否是全选状态，是则返回true，否则返回false
     * @param userId
     * @return
     */
    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

}
