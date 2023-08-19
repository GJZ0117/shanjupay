package com.shanjupay.merchant.api.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="StoreDTO", description="")
public class StoreDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class) // 进行json转换时转为string不再是long
    private Long id;

    @ApiModelProperty(value = "门店名称")
    private String storeName;

    @ApiModelProperty(value = "门店编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long storeNumber;

    @ApiModelProperty(value = "所属商户")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long merchantId;

    @ApiModelProperty(value = "父门店")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    @ApiModelProperty(value = "0表示禁用，1表示启用")
    private Boolean storeStatus;

    @ApiModelProperty(value = "门店地址")
    private String storeAddress;


}
