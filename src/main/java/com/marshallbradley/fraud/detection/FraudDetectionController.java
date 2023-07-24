package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.models.Response;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.marshallbradley.fraud.detection.FraudDetectionStreams.FRAUDULENT_TRANSACTION_COUNT_STORE;

@RestController
public class FraudDetectionController {

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @GetMapping("/fraudulent-transactions/{userId}")
    public ResponseEntity<Response> getFraudulentTransactions(@PathVariable String userId) {

        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, Long> fraudulentTransactions = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(FRAUDULENT_TRANSACTION_COUNT_STORE,
                        QueryableStoreTypes.keyValueStore())
        );
        Long transactions = fraudulentTransactions.get(userId);
        return ResponseEntity.ok(new Response(UUID.fromString(userId), (transactions != null) ? transactions : 0L));
    }
}
