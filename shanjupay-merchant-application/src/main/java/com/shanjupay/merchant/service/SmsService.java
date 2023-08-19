package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

public interface SmsService {
    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return 验证码对应的key
     */
    public String sendSms(String phone);

    /**
     * 校验手机验证码
     * @param verifyKey 验证码key
     * @param verifyCode 验证码
     */
    public void checkVerifyCode(String verifyKey, String verifyCode) throws BusinessException;
}
