package com.shanjupay.transaction.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.transaction.api.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "TP_PAYMENT_RESULT", consumerGroup = "CID_ORDER_CONSUMER")
public class TransactionPayConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    TransactionService transactionService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);
        log.info("交易服务接收到支付结果的消息:{}", jsonString);
        // 接收到的消息 内容包括订单状态
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(jsonString, PaymentResponseDTO.class);
        String tradeNo = paymentResponseDTO.getTradeNo(); // 支付宝、微信订单号
        String outTradeNo = paymentResponseDTO.getOutTradeNo(); // 订单号
        TradeStatus tradeState = paymentResponseDTO.getTradeState(); // 订单状态
        log.info("paymentResponseDTO: {}", paymentResponseDTO);
        log.info("tradeState: {}", tradeState);
        // 更新数据库
        switch (tradeState) {
            case SUCCESS: // 成功
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo, tradeNo, "2");
                return;
            case REVOKED: // 关闭
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo, tradeNo, "4");
                return;
            case FAILED: // 失败
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo, tradeNo, "5");
                return;
            default:
                throw new RuntimeException(String.format("无法解析支付结果: %s", body));
        }
    }
}
