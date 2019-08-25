package com.xmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.xmall.common.ServerResponse;
import com.xmall.service.IProductService;
import com.xmall.vo.ProductDetailVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/28 15:24
 * 前台商品
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /**
     * 获取前台商品细节
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return iProductService.getProductDetail(productId);
    }

    /**
     * @MethodName: detailRESTful
     * @Description: 获取前台商品细节，RESTful型接口
     * @Param: [productId]
     * @Return: com.xmall.common.ServerResponse<com.xmall.vo.ProductDetailVo>
     * @Author: rwxian
     * @Date: 2019/8/20 20:20
     */
    @RequestMapping(value = "/{productId}")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detailRESTful(@PathVariable Integer productId) {
        return iProductService.getProductDetail(productId);
    }

    /**
     * 根据关键字和分类id进行查询并分页，通过价格进行排序
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword", required = false) String keyword,
                                         @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "orderBy", defaultValue = "") String orderBy) {
        return iProductService.getProductByKeywordCatetory(keyword, categoryId, pageNum, pageSize, orderBy);
    }

    /**
     * @MethodName: listRESTful
     * @Description: 根据关键字和分类id进行查询并分页，RESTful型接口，不是所有接口都适合RESTful，比如此接口就不合适
     * @Param: [keyword, categoryId, pageNum, pageSize, orderBy]
     * @Return: com.xmall.common.ServerResponse<com.github.pagehelper.PageInfo>
     * @Author: rwxian
     * @Date: 2019/8/20 20:21
     */
    @RequestMapping(value = "/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}")
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "keyword") String keyword,
                                         @PathVariable(value = "categoryId") Integer categoryId,
                                         @PathVariable(value = "pageNum") Integer pageNum,
                                         @PathVariable(value = "pageSize") Integer pageSize,
                                         @PathVariable(value = "orderBy") String orderBy) {
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }

        return iProductService.getProductByKeywordCatetory(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
