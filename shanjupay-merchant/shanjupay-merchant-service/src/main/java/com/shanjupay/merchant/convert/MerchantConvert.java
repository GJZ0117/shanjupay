package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * 定义DTO和Entity转换规则
 */
@org.mapstruct.Mapper // 对象属性映射
public interface MerchantConvert {
    // 创建一个转换类实例
    MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

    // 把DTO转换为Entity
    Merchant dto2entity(MerchantDTO merchantDTO);

    // 把Entity转换为DTO
    MerchantDTO entity2dto(Merchant merchant);

    // 将Entity List转为DTO List
    List<MerchantDTO> entityList2dtoList(List<Merchant> merchantList);

    // 将DTO List转为Entity List
    List<Merchant> dtoList2entityList(List<MerchantDTO> merchantDTOList);

    // 测试
    public static void main(String[] args) {
        // 将DTO转为Entity
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setUsername("测试");
        merchantDTO.setMobile("123456789");
        Merchant dto2entity = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        System.out.println(dto2entity);

        // 将Entity转为DTO
        dto2entity.setMerchantName("商户名称");
        MerchantDTO entity2dto = MerchantConvert.INSTANCE.entity2dto(dto2entity);
        System.out.println(entity2dto);

        // 将Entity List转为DTO List
        List<Merchant> merchantList = new ArrayList<>();
        merchantList.add(dto2entity);
        List<MerchantDTO> merchantDTOList = MerchantConvert.INSTANCE.entityList2dtoList(merchantList);
        System.out.println(merchantDTOList);

    }
}
