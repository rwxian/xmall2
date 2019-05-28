package com.xmall.controller.backend;

import com.google.common.collect.Maps;
import com.xmall.common.Const;
import com.xmall.common.ResponseCode;
import com.xmall.common.ServerResponse;
import com.xmall.pojo.Product;
import com.xmall.pojo.User;
import com.xmall.service.IFileService;
import com.xmall.service.IProductService;
import com.xmall.service.IUserService;
import com.xmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/26 15:45
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 更新或添加产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {     // 判断角色是否为管理员
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createBySuccessMessage("对不起，没有权限操作！");
        }
    }

    /**
     * 更改产品上下架
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }
    }

    /**
     * 获取商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }
    }

    /**
     * 分页查询商品
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }
    }

    /**
     * 更具标题和产品id进行模糊查询
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId,
                                        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            // return iProductService.getProductList(pageNum, pageSize);
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }
    }

    /**
     * 后台商品图片上传
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file,
                                 HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要登录！");
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");   // 获取项目webapp下的upload的路径
            String targetFileName = iFileService.upload(file, path);    // 文件上传成功后，返回文件名
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName; // 拼接上传成功后图片的路径
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName); // 填入图片名称
            fileMap.put("url", url);            // 填入图片完整路径
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作！");
        }
    }

    /**
     * 使用simdirot插件做文件上传
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response) {
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("sucess", false);
            resultMap.put("msg", "请登录管理员！");
            return resultMap;
        }
        if (iUserService.checkAdminRole(user).isSucess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");   // 获取项目webapp下的upload的路径
            String targetFileName = iFileService.upload(file, path);    // 文件上传成功后，返回文件名

            if (StringUtils.isBlank(targetFileName)) {  // 上传失败
                resultMap.put("sucess", false);
                resultMap.put("msg", "文件上传失败！");
                return resultMap;
            }
            // 上传成功
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName; // 拼接上传成功后图片的路径
            resultMap.put("sucess", false);
            resultMap.put("msg", "文件上传成功！");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");   // simdirot需要设置此响应头
            return resultMap;
        } else {
            resultMap.put("sucess", false);
            resultMap.put("msg", "没有权限操作！");
            return resultMap;
        }
    }
}
