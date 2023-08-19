package com.shanjupay.paymentagent.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayProducer {

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    // 订单结果查询Topic
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";

    // 发送查询支付宝、微信订单状态的消息
    public void payOrderNotice(PaymentResponseDTO paymentResponseDTO) {
        // 发延迟消息
        Message<PaymentResponseDTO> message = MessageBuilder.withPayload(paymentResponseDTO).build();
        // 延迟第三级发送（延迟10秒）
        rocketMQTemplate.syncSend(TOPIC_ORDER, message, 1000, 3);
        log.info("支付渠道代理服务向mq发送订单查询消息: {}", JSON.toJSONString(paymentResponseDTO));
    }

    // 订单结果topic
    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";

    // 发送查询到的支付结果消息
    public void payResultNotice(PaymentResponseDTO paymentResponseDTO) {
        // 发送同步消息
        rocketMQTemplate.syncSend(TOPIC_RESULT, paymentResponseDTO);
        log.info("支付渠道代理服务向mq发送支付结果消息: {}", JSON.toJSONString(paymentResponseDTO));
    }
}
