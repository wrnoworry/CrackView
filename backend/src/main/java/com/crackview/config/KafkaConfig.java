package com.crackview.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaConfig {

    public static final String TOPIC_EVALUATION = "interview-evaluation";

    @Bean
    public NewTopic evaluationTopic() {
        return TopicBuilder.name(TOPIC_EVALUATION)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
