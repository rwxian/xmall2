package com.xmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.xmall.common.Const;
import com.xmall.common.ResponseCode;
import com.xmall.common.ServerResponse;
import com.xmall.dao.CategoryMapper;
import com.xmall.dao.ProductMapper;
import com.xmall.pojo.Category;
import com.xmall.pojo.Product;
import com.xmall.service.ICategoryService;
import com.xmall.service.IProductService;
import com.xmall.util.DateTimeUtil;
import com.xmall.util.PropertiesUtil;
import com.xmall.vo.ProductDetailVo;
import com.xmall.vo.ProductListVo;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/26 15:49
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 保存或更新产品
     * @param product
     * @return
     */
    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {      // 产品不为空
            if (StringUtils.isNotBlank(product.getSubImages())) {  // 如果子图不为空
                String[] subImgArray = product.getSubImages().split(","); // 获取每张图片
                if (subImgArray.length > 0) {   // 图片存在
                    product.setMainImage(subImgArray[0]); // 把第一张图设为主图
                }
            }

            if (product.getId() != null) {  // 产品id不为空，说明是更新
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("更新产品成功！");
                }
                return ServerResponse.createBySuccessMessage("更新产品失败！");
            } else {    // 产品id为空，说明是新增
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("新增产品成功！");
                }
                return ServerResponse.createBySuccessMessage("新增产品失败！");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数不正确！");
    }

    /**
     * 修改产品销售状态
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功！");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败！");
    }

    /**
     * 管理产品详情
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {    // 传递的参数为空
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或删除！");
        }
        ProductDetailVo productDetailVo = assembProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 把Product对象封装成value-object
     * @param product
     * @return
     */
    public ProductDetailVo assembProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubTitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category != null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    /**
     * 分页查询商品
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);    // 设置分页起始行
        List<Product> productsList = productMapper.selectList(); // 查询出所有产品

        List<ProductListVo> productListVoList = Lists.newArrayList(); // 声明一个list集合，并初始化
        for (Product productItem :
                productsList) {
            ProductListVo productListVo = assembleProductListVo(productItem);   // 把查询的所有Product对象封装成ProductListVo，过滤掉不需要的字段
            productListVoList.add(productListVo);   // 把封装好的ProductListVo添加到list中
        }
        PageInfo pageResult = new PageInfo(productsList);   // 通过PageInfo的构造对list中的ProductListVo进行自动分页处理，可得到多少页、多少记录等信息
        pageResult.setList(productListVoList);  // 重置productListVo集合,为下次分页做准备
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubTitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    /**
     * 分页+模糊查询
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);    // 设置分页起始行

        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();// 拼接sql需要的%
        }
        System.out.println("用于搜索的产品关键字为：" + productName);

        List<Product> productsList = productMapper.selectByNameAndProductId(productName, productId); // 查询出所有产品

        List<ProductListVo> productListVoList = Lists.newArrayList(); // 声明一个list集合，并初始化
        for (Product productItem : productsList) {
            ProductListVo productListVo = assembleProductListVo(productItem);   // 把查询的所有Product对象封装成ProductListVo，过滤掉不需要的字段
            System.out.println("--------" + productListVo);
            productListVoList.add(productListVo);   // 把封装好的ProductListVo添加到list中
        }
        PageInfo pageResult = new PageInfo(productsList);   // 通过PageInfo的构造对list中的ProductListVo进行自动分页处理，可得到多少页、多少记录等信息
        pageResult.setList(productListVoList);  // 重置productListVo集合,为下次分页做准备
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 前台获取商品细节
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {    // 传递的参数为空
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品已下架或删除！");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("商品已下架或删除！");
        }
        ProductDetailVo productDetailVo = assembProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 根据关键字和分类id进行分页查询
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeywordCatetory(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {    // 关键字和分类id为空
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) { // 查询结果为空且关键字为空
                PageHelper.startPage(pageNum, pageSize);    // 设置分页起始行
                List<ProductListVo> productListVoList = Lists.newArrayList(); // 声明一个list集合，并初始化
                PageInfo pageInfo = new PageInfo(productListVoList);    // 封装一个空结果
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData(); // 根据分类id查询出此id下的所有子分类
        }
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum, pageSize);
        // 排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_"); // 根据下划线分割price_asc/price_desc
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]); // 拼接为 price order by asc的格式
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword,
                categoryIdList.size() == 0 ? null : categoryIdList);    // 执行查询

        // 对查询结果分页
        List<ProductListVo> productListVoList = Lists.newArrayList(); // 声明一个list集合，并初始化
        for (Product productItem :
                productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);   // 把查询的所有Product对象封装成ProductListVo，过滤掉不需要的字段
            productListVoList.add(productListVo);   // 把封装好的ProductListVo添加到list中
        }
        PageInfo pageInfo = new PageInfo(productList);   // 通过PageInfo的构造对list中的ProductListVo进行自动分页处理，可得到多少页、多少记录等信息
        pageInfo.setList(productListVoList);  // 重置productListVo集合,为下次分页做准备
        return ServerResponse.createBySuccess(pageInfo);
    }
}
