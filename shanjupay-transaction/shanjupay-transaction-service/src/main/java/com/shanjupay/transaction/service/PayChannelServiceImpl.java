package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.apache.dubbo.config.annotation.Service
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    PlatformChannelMapper platformChannelMapper;

    @Autowired
    AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    PayChannelParamMapper payChannelParamMapper;

    @Autowired
    Cache cache;

    // 查询平台服务类型
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        // 查询platform_channel表中全部记录
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        // 将PlatformChannel转为包含dto的list
        return PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
    }

    /**
     * 为某个应用绑定一个服务类型
     *
     * @param appId                应用id
     * @param platformChannelCodes 服务类型code
     * @throws BusinessException
     */

    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        // 根据应用id和服务类型code查询，如果已经绑定则不再插入，否则插入记录
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        if (appPlatformChannel == null) {
            // 向app_platform_channel插入
            AppPlatformChannel entity = new AppPlatformChannel();
            entity.setAppId(appId);
            entity.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(entity);
        }

    }

    /**
     * 查询应用绑定服务类型状态
     *
     * @param appId
     * @param platformChannel
     * @return 1:已绑定 0:未绑定
     * @throws BusinessException
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        if (appPlatformChannel != null) {
            return 1;
        }
        return 0;
    }

    /**
     * 根据服务类型查询支付渠道
     *
     * @param platformChannelCode 服务类型编码
     * @return 支付渠道列表
     * @throws BusinessException
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        // 调用mapper查询数据库platform_pay_channel, pay_channel, platform_channel
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    /**
     * 支付渠道参数配置
     *
     * @param payChannelParamDTO 配置支付渠道参数 (商户id，应用id，服务类型code，支付渠道code，配置名称，配置参数json)
     * @throws BusinessException
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException {
        if (payChannelParamDTO == null || payChannelParamDTO.getChannelName() == null || payChannelParamDTO.getParam() == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        // 根据 应用、服务类型、支付渠道 查询记录
        // 根据 应用、服务类型 查询 应用与服务类型绑定的id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
        if (appPlatformChannelId == null) {
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据 应用与服务类型绑定的id和支付渠道 查询 PayChannelParam 的一条记录
        PayChannelParam entity = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                .eq(PayChannelParam::getPayChannel, payChannelParamDTO.getPayChannel()));
        // 如果存在记录则更新，否则添加
        if (entity != null) { // 更新
            entity.setChannelName(payChannelParamDTO.getChannelName()); // 配置名称
            entity.setParam(payChannelParamDTO.getParam()); // 配置json格式参数
            payChannelParamMapper.updateById(entity);
        } else { // 添加
            PayChannelParam newEntity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            newEntity.setId(null); // mybatis-plus会自动生成主键
            newEntity.setAppPlatformChannelId(appPlatformChannelId);
            payChannelParamMapper.insert(newEntity);
        }

        // 向redis缓存支付渠道参数
        updateCache(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
    }

    /**
     * 根据应用、服务类型查询支付渠道参数列表
     * @param appId           应用id
     * @param platformChannel 服务类型
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {
        // 先从redis查，如果查到了直接返回，否则从数据库查，从数据库查询完毕再将数据保存到redis
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        Boolean exists = cache.exists(redisKey);
        if (exists) {
            // 从redis获取支付参数列表
            String payChannelParamDTO_str = cache.get(redisKey);
            // 将json串转为列表
            List<PayChannelParamDTO> payChannelParamDTOS = JSON.parseArray(payChannelParamDTO_str, PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }
        // 根据应用和服务类型找到绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if (appPlatformChannelId == null) {
            return null;
        }
        // 应用和服务类型绑定id查询支付渠道参数表记录
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
        // 保存到redis
        if (payChannelParamDTOS != null) {
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }
        return payChannelParamDTOS;
    }

    /**
     * 根据应用、服务类型和支付渠道代码查询该支付渠道的参数配置信息
     *
     * @param appId           应用id
     * @param platformChannel 服务类型
     * @param payChannel      支付渠道代码
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        // 根据应用、服务类型查询支付渠道参数列表
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        for (PayChannelParamDTO payChannelParamDTO : payChannelParamDTOS) {
            if (payChannelParamDTO.getPayChannel().equals(payChannel)) {
                return payChannelParamDTO;
            }
        }
        return null;
    }

    /**
     * 根据 应用、服务类型 查询 应用与服务类型绑定的id
     */
    private Long selectIdByAppPlatformChannel(String appId, String platformChannelCode) {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel != null) {
            return appPlatformChannel.getId(); // 应用与服务类型的绑定id
        }
        return null;
    }

    /**
     * 根据应用id和服务类型code查询支付渠道列表，将支付渠道参数列表写入redis
     *
     * @param appId               应用id
     * @param platformChannelCode 服务类型code
     */
    private void updateCache(String appId, String platformChannelCode) {
        // 得到redis中key（支付渠道参数列表的key） SJ_PAY_PARAM:应用id:服务类型code
        String redisKey = RedisUtil.keyBuilder(appId, platformChannelCode);
        // 根据key查询redis
        Boolean exists = cache.exists(redisKey);
        if (exists) {
            cache.del(redisKey);
        }
        // 根据应用id和服务类型code查询支付渠道列表
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannelCode);
        // 将支付渠道参数列表转为json存入redis
        if (payChannelParamDTOS != null) {
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }
    }
}
