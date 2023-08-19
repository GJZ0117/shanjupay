package com.shanjupay.test.rocketmq.message;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "my-topic", consumerGroup = "demo-consumer-group")
public class ConsumerSimple implements RocketMQListener<String> {

    // 得到消息
    @Override
    public void onMessage(String msg) {
        // 编写业务逻辑 （此方法被调用表示接收到了消息）
        System.out.println(msg);
    }
}
