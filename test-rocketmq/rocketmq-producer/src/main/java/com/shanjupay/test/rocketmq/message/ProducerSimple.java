package com.shanjupay.test.rocketmq.message;

import com.shanjupay.test.rocketmq.model.OrderExt;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ProducerSimple {
    @Autowired
    RocketMQTemplate rocketMQTemplate;

    /**
     * 发送同步消息
     *
     * @param topic 主题
     * @param msg   消息内容
     */
    public void sendSyncMsg(String topic, String msg) {
        rocketMQTemplate.syncSend(topic, msg);
    }

    /**
     * 异步消息
     *
     * @param topic
     * @param msg
     */
    public void sendASyncMsg(String topic, String msg) {
        rocketMQTemplate.asyncSend(topic, msg, new SendCallback() {
            // 消息发送成功的回调
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println(sendResult);
            }

            // 消息发送失败的回调
            @Override
            public void onException(Throwable throwable) {
                System.out.println(throwable);
            }
        });
    }

    /**
     * 发送对象(同步)
     *
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJson(String topic, OrderExt orderExt) {
        // 将对象转为json串发
        rocketMQTemplate.convertAndSend(topic, orderExt);
    }

    // 发送延迟消息
    public void sendMsgByJsonDelay(String topic, OrderExt orderExt) {
        Message<OrderExt> message = MessageBuilder.withPayload(orderExt).build();
        rocketMQTemplate.syncSend(topic, message, 1000, 3);
    }
}
