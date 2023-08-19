package com.shanjupay.merchant.vo;

import com.shanjupay.transaction.api.dto.PayOrderDTO;
import lombok.Data;

import java.util.List;
// 前端bill.vue页面查询所返回的内容
@Data
public class PayOrderVo {
    private List<PayOrderDTO> payOrderDTOList; // 列表数据
    private Integer total; // 数据库中记录总条数
    private Integer pageNo;
    private Integer pageSize;
}
