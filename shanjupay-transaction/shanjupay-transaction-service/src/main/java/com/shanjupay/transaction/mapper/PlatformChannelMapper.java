package com.shanjupay.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.entity.PlatformChannel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  Mapper 接口
 */
@Repository
public interface PlatformChannelMapper extends BaseMapper<PlatformChannel> {
    /**
     * 根据服务类型code查询对应的支付渠道信息
     * @param platformChannelCode
     * @return
     */
    @Select("SELECT pc.* FROM platform_pay_channel ppc, pay_channel pc, platform_channel pla WHERE ppc.pay_channel=pc.channel_code AND ppc.platform_channel=pla.channel_code and pla.channel_code=#{platformChannelCode}")
    public List<PayChannelDTO> selectPayChannelByPlatformChannel(String platformChannelCode);
}
