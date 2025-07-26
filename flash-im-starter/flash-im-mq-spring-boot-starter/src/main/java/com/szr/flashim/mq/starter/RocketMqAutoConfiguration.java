package com.szr.flashim.mq.starter;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@ConditionalOnProperty(name = "rocketmq.name-server")
public class RocketMqAutoConfiguration {
    @Bean
    public MqClientManager mqClientManager(RocketMQProducer rocketMQProducer) {
        // 配置RocketMQTemplate
        return new MqClientManager(rocketMQProducer);
    }

    @Bean
    public RocketMQProducer rocketMQProducer(RocketMQTemplate rocketMQTemplate) {
        // 创建RocketMQProducer
        return new RocketMQProducer(rocketMQTemplate);
    }
}
