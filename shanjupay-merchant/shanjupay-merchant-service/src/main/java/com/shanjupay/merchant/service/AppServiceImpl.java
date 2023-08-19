package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppConvert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@org.apache.dubbo.config.annotation.Service
public class AppServiceImpl implements AppService {

    @Autowired
    AppMapper appMapper;

    @Autowired
    MerchantMapper merchantMapper;

    /**
     * 创建应用
     *
     * @param merchantId 商户id
     * @param appDTO     应用信息
     * @return 创建成功的应用信息
     * @throws BusinessException
     */
    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {
        // 检验参数
        if (merchantId == null || appDTO == null || StringUtils.isBlank(appDTO.getAppName())) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        // 取出商户资质申请状态
        String auditStatus = merchant.getAuditStatus();
        if (!"2".equals(auditStatus)) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        // 校验应用名称唯一
        String appName = appDTO.getAppName();
        Boolean exitAppName = isExitAppName(appName);
        if (exitAppName) {
            throw new BusinessException(CommonErrorCode.E_200004);
        }
        // 生成应用id
        String appId = UUID.randomUUID().toString();
        App entity = AppConvert.INSTANCE.dto2entity(appDTO);
        entity.setAppId(appId);
        entity.setMerchantId(merchantId);
        // 调用appMapper向应用表插入数据
        appMapper.insert(entity);
        return AppConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {
        List<App> appList = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        return AppConvert.INSTANCE.listEntity2dto(appList);
    }

    @Override
    public AppDTO getAppById(String appId) throws BusinessException {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, appId));
        return AppConvert.INSTANCE.entity2dto(app);
    }

    /**
     * 校验应用是否属于商户
     *
     * @param appId
     * @param merchantId
     * @return true 表示存在 false 表示不存在
     * @throws BusinessException
     */
    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) throws BusinessException {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>()
                .eq(App::getAppId, appId)
                .eq(App::getMerchantId, merchantId));

        return count > 0;
    }

    /**
     * 校验应用名是否已被使用
     */
    public Boolean isExitAppName(String name) {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, name));
        return count > 0;
    }
}
