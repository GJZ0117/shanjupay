package com.shanjupay.merchant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("store")
public class Store implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 门店名称
     */
    @TableField("STORE_NAME")
    private String storeName;

    /**
     * 门店编号
     */
    @TableField("STORE_NUMBER")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long storeNumber;

    /**
     * 所属商户
     */
    @TableField("MERCHANT_ID")
    private Long merchantId;

    /**
     * 父门店
     */
    @TableField("PARENT_ID")
    private Long parentId;

    /**
     * 0表示禁用，1表示启用
     */
    @TableField("STORE_STATUS")
    private Boolean storeStatus;

    /**
     * 门店地址
     */
    @TableField("STORE_ADDRESS")
    private String storeAddress;


}
