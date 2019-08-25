package com.xmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/28 10:40
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
