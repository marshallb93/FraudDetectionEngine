package com.marshallbradley.fraud.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Transaction {

    UUID id;
    UUID userId;
    UUID destinationId;
    Integer amount;
    LocalDateTime timestamp;

    public Transaction(UUID userId, UUID destinationId, Integer amount, LocalDateTime timestamp) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.destinationId = destinationId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public static KeyValueMapper<Windowed<String>, ArrayList<Transaction>, Iterable<KeyValue<String, FraudulentTransaction>>> transactionsToFraudulentTransactionsMapper() {
        return (key, value) -> {
            List<KeyValue<String, FraudulentTransaction>> result = new ArrayList<>(value.size());
            for (int i = 0; i < value.size(); i++) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                Transaction transaction = objectMapper.convertValue(value.get(i), Transaction.class);
                result.add(new KeyValue<>(transaction.getId().toString(), new FraudulentTransaction(transaction, FraudType.HIGH_VOLUME)));
            }
            return result;
        };
    }
}
