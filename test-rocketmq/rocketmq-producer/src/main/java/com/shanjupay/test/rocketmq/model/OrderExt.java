package com.shanjupay.test.rocketmq.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class OrderExt {
    private String id;

    private Date createTime;

    private Long money;

    private String title;
}
