package com.shanjupay.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.transaction.api.vo.DealVo;
import com.shanjupay.transaction.entity.PayOrder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 *  Mapper 接口
 */
@Repository
public interface PayOrderMapper extends BaseMapper<PayOrder> {
    // 某商户下所有应用的交易汇总(查询全部交易信息)
    public DealVo getDealCollect(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);

    // 商户下所有交易按照应用分类汇总
    public DealVo getAllDealFromAppType(String appId, LocalDateTime startTime, LocalDateTime endTime);

    // 商户下所有交易按渠道汇总
    public DealVo getAllDealFromChannel(String appId, String channel, LocalDateTime startTime, LocalDateTime endTime);
}
