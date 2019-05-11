package com.xmall.service;

import com.xmall.common.ServerResponse;
import com.xmall.pojo.Category;

import java.util.List;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/25 22:34
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer parentId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallerCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
