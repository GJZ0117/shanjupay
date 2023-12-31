package com.shanjupay.test.rocketmq.message;

import com.shanjupay.test.rocketmq.model.OrderExt;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "my-topic-obj", consumerGroup = "demo-consumer-group-obj")
public class ConsumerSimpleObj implements RocketMQListener<MessageExt> {
    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);
        System.out.println(messageExt);
        int reconsumeTimes = messageExt.getReconsumeTimes();
        if (reconsumeTimes > 2 ) {
            // 将此消息加入数据库,由单独的程序进行人工干预
        }
        if (1==1) {
            throw new RuntimeException("消息处理失败!");
        }
    }
}
