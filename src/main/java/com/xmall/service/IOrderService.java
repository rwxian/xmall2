package com.xmall.service;

import com.xmall.common.ServerResponse;

import java.util.Map;

/**
 * @author rwxian@foxmail.com
 * @date 2019/5/9 12:39
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
