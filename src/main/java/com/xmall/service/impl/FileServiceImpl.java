package com.xmall.service.impl;

import com.google.common.collect.Lists;
import com.xmall.service.IFileService;
import com.xmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/28 10:40
 * 文件上传服务类
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 文件上传服务
     * @param file
     * @param path
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();   // 获取上传文件的原文件名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1); // 先根据lastIndexOf获取文件后缀的索引位置，然后从此位置截取到最后一位，得到文件扩展名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;   // 在服务器中存储的文件的名字
        logger.info("开始上传文件，上传文件的文件名为{}，上传路径为{}，新文件名为{}！", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()) {    // 如果文件夹不存在
            fileDir.setWritable(true);  // 设置写权限
            fileDir.mkdirs();    // 创建文件夹
        }
        File targetFile = new File(path, uploadFileName);   // 创建path文件夹，并把uploadFileName放入其中
        try {
            file.transferTo(targetFile);    // 把文件保存到项目的webapp下的path路径下

            FTPUtil.uploadFile(Lists.newArrayList(targetFile));// 将targetFile上传到ftp服务器

            targetFile.delete(); // 上传完成，删除path(upload)下的文件
        } catch (IOException e) {
            logger.error("文件上传异常！", e);

        }
        return targetFile.getName();    // 返回上传文件的文件名
    }
}
