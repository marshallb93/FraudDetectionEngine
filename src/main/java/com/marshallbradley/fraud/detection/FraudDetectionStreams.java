package com.marshallbradley.fraud.detection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marshallbradley.fraud.detection.models.FraudType;
import com.marshallbradley.fraud.detection.models.FraudulentTransaction;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class FraudDetectionStreams {


    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        Serde<User> userSerde = new JsonSerde<>(User.class);
        Serde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
        Serde<FraudulentTransaction> fraudulentTransactionSerde  = new JsonSerde<>(FraudulentTransaction.class);

        KStream<String, Transaction> transactionStream = streamsBuilder
                .stream("transactions", Consumed.with(null, transactionSerde));

        KTable<String, User> userTable = streamsBuilder.table("users", Consumed.with(null, userSerde));

        KStream<String, Transaction> transactionByUserIdStream = transactionStream
                .map((key, transaction) -> new KeyValue<>(String.valueOf(transaction.getUserId()), transaction));

        KStream<String, Pair<Transaction, User>> transactionsWithUserStream = transactionByUserIdStream.join(userTable, Pair::of);

        // Detect transactions which overspend a user's single transaction limit
        transactionsWithUserStream.filter((key, transactionWithUser) -> transactionWithUser.getLeft().getAmount() >
                        transactionWithUser.getRight().getSingleTransactionLimit())
        .map((key, value) -> new KeyValue<>(value.getLeft().getId().toString(),
                new FraudulentTransaction(value.getLeft(), FraudType.OVERSPEND)))
        .to("fraudulent-transactions", Produced.with(null, fraudulentTransactionSerde));

        KeyValueMapper<Windowed<String>, ArrayList<Transaction>, Iterable<KeyValue<String, FraudulentTransaction>>> keyValueMapper
                = (key, value) -> {
                    List<KeyValue<String, FraudulentTransaction>> result = new ArrayList<>(value.size());
                    for (int i = 0; i < value.size(); i++) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.registerModule(new JavaTimeModule());
                        Transaction transaction = objectMapper.convertValue(value.get(i), Transaction.class);
                        result.add(new KeyValue<>(transaction.getId().toString(), new FraudulentTransaction(transaction, FraudType.HIGH_VOLUME)));
                    }
                    return result;
                };

        // Detect transactions which overspend a user's single transaction limit
        transactionsWithUserStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(5)))
                .aggregate(ArrayList<Transaction>::new, (aggKey, newValue, aggValue) ->
                    {aggValue.add(newValue.getLeft()); return aggValue;},
                    Materialized.with(Serdes.String(), new JsonSerde<>(ArrayList.class)))
                .toStream()
                .filter((key, list) -> list.size() >= 5)
                .flatMap(keyValueMapper)
                .to("fraudulent-transactions", Produced.with(null, fraudulentTransactionSerde));

//        transactionStream
//                .groupByKey()
//                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
//                .count()
//                .toStream()
//                .filter((key, count) -> count >= transactionCountThreshold)
//                .to("fraudulent-transactions");
    }
}
