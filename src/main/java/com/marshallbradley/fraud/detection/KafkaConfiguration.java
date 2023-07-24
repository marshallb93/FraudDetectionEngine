package com.marshallbradley.fraud.detection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;


@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfiguration {

    @Value("${spring.application.name}")
    private String applicationId;

    @Value(value = "${kafka.bootstrap-servers}")
    private String bootstrapAddress;
}
