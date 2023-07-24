package com.marshallbradley.fraud.detection;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fraud")
public class FraudDetectionController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public FraudDetectionController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/users")
    public List<String> getUsersWithFraudulentTransactions() {
        //TODO
    }

    @GetMapping("/transactions/{userId}")
    public List<Transaction> getTransactionsForUser(@PathVariable String userId) {
        //TODO
    }
}
