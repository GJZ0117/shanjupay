package com.shanjupay.transaction.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(value = "DealVo", description = "前端deal.vue页面所需传递的数据")
@Data
public class DealVo {
    private String appName; // 应用名称
    private String channelName; // 渠道名称
    private Integer totalAmount; // 总交易额
    private Integer successAmount; // 成功交易金额
    private Integer requestNumber; // 发起笔数
    private Integer successNumber; // 成功笔数
    private Double conversion; // 转化率
    private Double avgAmount; // 平均订单金额
}
