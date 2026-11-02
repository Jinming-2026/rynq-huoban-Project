package com.yz.minded.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
public class AlOssUtil {
    // 获取 阿里云OSS 的 Bucket 域名
    public static final String ALI_DOMAIN = "your_url";
//    private static final Logger log = LoggerFactory.getLogger(AlOssUtil.class);

    public static String uploadImage(MultipartFile file,String fileName){
        // 生成一个新的文件名
        // 获取地域节点
        String endpoint = "your_url";
        String accessKeyId = "your_id";
        String accessKeySecret = "your_secret";
        // 创建 OSS 客户端对象
        OSS ossClient = new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);
        // 通过该对象上传图片
        try {
            ossClient.putObject(
                    "yz-minded",    // 仓库名
                    fileName,   // 文件名
                    file.getInputStream()
            );
            // 上传完毕之后关闭掉 ossClient
            ossClient.shutdown();
        } catch (IOException e) {
            log.error("文件上传失败：",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 返回文件地址（文件 ip 地址）
        return ALI_DOMAIN + fileName;
    }




}
