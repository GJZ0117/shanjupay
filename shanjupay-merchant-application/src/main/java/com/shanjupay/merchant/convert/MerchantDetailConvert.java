package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import org.mapstruct.factory.Mappers;

/**
 * 商户资质申请vo和dto进行转换
 */

@org.mapstruct.Mapper
public interface MerchantDetailConvert {
    MerchantDetailConvert INSTANCE = Mappers.getMapper(MerchantDetailConvert.class);

    // 将dto转为vo
    public MerchantDetailVO dto2vo(MerchantDTO merchantDTO);
    // 将vo转为dto
    public MerchantDTO vo2dto(MerchantDetailVO merchantDetailVO);
}
