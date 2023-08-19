package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

@org.springframework.stereotype.Service
public interface FileService {
    /**
     * 上传文件
     * @param bytes 文件字节数组
     * @param fileName 文件名
     * @return 文件访问路径 url
     * @throws BusinessException
     */
    public String upload(byte[] bytes, String fileName) throws BusinessException;
}
