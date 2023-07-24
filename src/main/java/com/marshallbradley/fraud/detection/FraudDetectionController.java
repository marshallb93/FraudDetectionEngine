package com.marshallbradley.fraud.detection;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

@RestController
public class FraudDetectionController {

    @Autowired
    private StreamsBuilder streamsBuilder;

    @Autowired
    private StreamsBuilderFactoryBean factoryBean;

    @GetMapping("/fraudulent-transactions/{account}")
    public ResponseEntity<Integer> getFraudulentTransactions(@PathVariable String account) {

        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, Long> fraudulentTransactions = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("fraudulent-transactions-count", QueryableStoreTypes.keyValueStore())
        );
        ResponseEntity response;
        if (fraudulentTransactions.get(account) == null) {
            response = ResponseEntity.notFound().build();
        } else {
            response = ResponseEntity.ok(fraudulentTransactions.get(account));
        }
        return response;
    }
}
