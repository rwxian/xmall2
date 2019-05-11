package com.xmall.service;

import com.xmall.common.ServerResponse;
import com.xmall.pojo.Shipping;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/29 9:47
 */
public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse delete(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse list(Integer userId, int pageNum, int pageSize);
}
