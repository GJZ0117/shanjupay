package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/**
 * 应用管理相关接口
 */

public interface AppService {
    /**
     * 创建应用
     *
     * @param merchantId 商户id
     * @param appDTO     应用信息
     * @return 创建成功的应用信息
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException;

    /**
     * 根据商户id查询应用列表
     *
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;

    /**
     * 根据应用id查询应用信息
     *
     * @param appId
     * @return
     * @throws BusinessException
     */
    AppDTO getAppById(String appId) throws BusinessException;

    /**
     * 校验应用是否属于商户
     *
     * @param appId
     * @param merchantId
     * @return true 表示存在 false 表示不存在
     * @throws BusinessException
     */
    Boolean queryAppInMerchant(String appId, Long merchantId) throws BusinessException;
}
