package com.xmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.xmall.common.ServerResponse;
import com.xmall.dao.ShippingMapper;
import com.xmall.pojo.Shipping;
import com.xmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/29 9:47
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 添加收货地址
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0) {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId()); // 插入成功后，立即获取id
            return ServerResponse.createBySuccess("新添收获地址成功！", result); // 返回的result是收货地址id
        }
        return ServerResponse.createByErrorMessage("新建地址失败！");
    }

    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse delete(Integer userId, Integer shippingId) {
        int rowCount = shippingMapper.deleteByShippingIdUserId(userId, shippingId);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("删除收获地址成功！");
        }
        return ServerResponse.createByErrorMessage("删除地址失败！");
    }

    /**
     * 更新收货地址
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);  // 设置id为当前登录用户的id
        int rowCount = shippingMapper.updateByShipping(shipping);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("更新收获地址成功！");
        }
        return ServerResponse.createByErrorMessage("更新地址失败！");
    }

    /**
     * 查询收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse select(Integer userId, Integer shippingId) {
        Shipping rowCount = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if (rowCount == null) {
            return ServerResponse.createBySuccessMessage("无法查询到该地址！");
        }
        return ServerResponse.createBySuccess("查询地址成功！",rowCount);
    }

    /**
     * 分页查询
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippings = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippings);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
