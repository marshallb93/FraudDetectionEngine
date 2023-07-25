package com.marshallbradley.fraud.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marshallbradley.fraud.models.FraudType;
import com.marshallbradley.fraud.models.FraudulentTransaction;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.ArrayList;
import java.util.List;

public class Serdes {
    public static final Serde<User> USER_SERDE = new JsonSerde<>(User.class);
    public static final Serde<Transaction> TRANSACTION_SERDE = new JsonSerde<>(Transaction.class);
    public static final Serde<FraudulentTransaction> FRAUDULENT_TRANSACTION_SERDE = new JsonSerde<>(FraudulentTransaction.class);

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
