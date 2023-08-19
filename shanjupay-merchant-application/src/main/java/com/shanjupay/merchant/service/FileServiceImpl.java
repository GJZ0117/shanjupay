package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class FileServiceImpl implements FileService {
    @Value("${oss.qiniu.url}")
    String url;
    @Value("${oss.qiniu.accessKey}")
    String accessKey;
    @Value("${oss.qiniu.secretKey}")
    String secretKey;
    @Value("${oss.qiniu.bucket}")
    String bucket;

    /**
     * 上传文件
     * @param bytes 文件字节数组
     * @param fileName 文件名
     * @return 文件访问路径 url
     * @throws BusinessException
     */
    @Override
    public String upload(byte[] bytes, String fileName) throws BusinessException {
        // 调用common下的工具类
        try {
            QiniuUtils.upload2qiniu(accessKey, secretKey, bucket, bytes, fileName);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        // 上传成功返回文件访问地址 绝对路径（七牛url+文件名）
        return url + fileName;
    }
}
