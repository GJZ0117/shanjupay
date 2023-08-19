package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.factory.Mappers;

/**
 * 将商户注册vo和dto进行转换
 */
@org.mapstruct.Mapper
public interface MerchantRegisterConvert {
    MerchantRegisterConvert INSTANCE = Mappers.getMapper(MerchantRegisterConvert.class);

    // 将DTO转为vo
    MerchantRegisterVO dto2vo(MerchantDTO merchantDTO);
    // 将vo转为DTO
    MerchantDTO vo2dto(MerchantRegisterVO merchantRegisterVO);
}
