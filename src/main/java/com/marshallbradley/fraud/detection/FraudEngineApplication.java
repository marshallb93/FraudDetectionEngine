package com.marshallbradley.fraud.detection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class FraudEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudEngineApplication.class, args);
    }
}
