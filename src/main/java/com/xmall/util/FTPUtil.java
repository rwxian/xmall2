package com.xmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author rwxian@foxmail.com
 * @date 2019/4/28 11:18
 * 上传图片到ftp服务器的工具类
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;      // ftp服务器的ip地址
    private int port;       // 端口
    private String user;    // 用户名
    private String pwd;     // 密码
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * 对外提供的文件上传方法
     * @param fileList
     * @return
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass); // 创建对象
        logger.info("开始连接ftp服务器！");
        boolean result = ftpUtil.uploadFile("img", fileList);   // 上传文件
        return result;
    }

    /**
     * 具体的文件上传方法
     * @param remotePath
     * @param fileList
     * @return
     */
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {   // 如果连接服务器成功
            try {
                ftpClient.changeWorkingDirectory(remotePath);   // 改变工作目录
                ftpClient.setBufferSize(1024);  // 设置缓冲区大小
                ftpClient.setControlEncoding("UTF-8");  // 设置字符编码
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  // 设置文件格式为二进制，防止乱码
                ftpClient.enterLocalPassiveMode();  // 打开本地被动模式

                for (File fileItem :
                        fileList) {
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fis);   // 把文件保存到ftp服务器
                }
            } catch (IOException e) {
                logger.error("文件上传异常！", e);
                uploaded = false;       // 发生异常，置为false
            } finally {     // 关流和关连接
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    /**
     * 连接ftp服务器
     * @param ip
     * @param port
     * @param user
     * @param pwd
     * @return
     */
    private boolean connectServer(String ip, int port, String user, String pwd) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);  // 连接ftp服务器
            isSuccess = ftpClient.login(user, pwd); // 登录ftp服务器
        } catch (IOException e) {
            logger.error("连接ftp服务器失败！", e);
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }


}
