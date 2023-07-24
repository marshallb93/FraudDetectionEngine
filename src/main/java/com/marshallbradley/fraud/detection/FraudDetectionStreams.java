package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.detection.models.FraudType;
import com.marshallbradley.fraud.models.FraudulentTransaction;
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
import java.time.LocalDateTime;
import java.util.ArrayList;

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

        // Detect transactions which overspend a user's single transaction limit
        transactionsWithUserStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(5)))
                .aggregate(ArrayList<Transaction>::new, (aggKey, newValue, aggValue) ->
                    {aggValue.add(newValue.getLeft()); return aggValue;},
                    Materialized.with(Serdes.String(), new JsonSerde<>(ArrayList.class)))
                .toStream()
                .filter((key, list) -> list.size() >= 5)
                .flatMap(Transaction.transactionsToFraudulentTransactionsMapper())
                .to("fraudulent-transactions", Produced.with(null, fraudulentTransactionSerde));

        // Detect transactions which have a timestamp outside of a 30 second grace period
        transactionStream
                .filter((key, value) -> value.getTimestamp().isAfter(LocalDateTime.now().plusSeconds(30)) ||
                                        value.getTimestamp().isBefore(LocalDateTime.now().minusSeconds(30)))
                .map((key, value) -> new KeyValue<>(key, new FraudulentTransaction(value, FraudType.INVALID_TIME)))
                .to("fraudulent-transactions", Produced.with(null, fraudulentTransactionSerde));

        streamsBuilder
                .stream("fraudulent-transactions", Consumed.with(null, fraudulentTransactionSerde))
                .map((key, transaction) -> new KeyValue<>(String.valueOf(transaction.getTransaction().getUserId()), transaction))
                .groupByKey()
                .count(Materialized.with(Serdes.String(), Serdes.Integer()).as("fraudulent-transactions-count"));

    }
}
