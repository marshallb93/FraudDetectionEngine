package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.models.FraudType;
import com.marshallbradley.fraud.models.FraudulentTransaction;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.marshallbradley.fraud.kafka.serdes.Serdes.*;

@Component
public class FraudDetectionStreams {

    public static final String FRAUDULENT_TRANSACTION_COUNT_STORE = "fraudulent-transactions-count";
    public static final String TRANSACTIONS_TOPIC = "transactions";
    public static final String FRAUDULENT_TRANSACTIONS_TOPIC = "fraudulent-transactions";
    public static final String USERS_TOPIC = "users";

    @Value("${spring.application.parameters.invalid-time.grace-period}")
    private Integer gracePeriod;

    @Value(value = "${spring.application.parameters.high-volume.threshold}")
    private Integer threshold;

    @Value(value = "${spring.application.parameters.high-volume.time-window}")
    private Integer timeWindow;


    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        KStream<String, Transaction> transactionStream = streamsBuilder
                .stream(TRANSACTIONS_TOPIC, Consumed.with(null, TRANSACTION_SERDE));

        KTable<String, User> userTable = streamsBuilder.table(USERS_TOPIC, Consumed.with(null, USER_SERDE));

        KStream<String, Transaction> transactionByUserIdStream = transactionStream
                .map((key, transaction) -> new KeyValue<>(String.valueOf(transaction.getUserId()), transaction));

        KStream<String, Pair<Transaction, User>> transactionsWithUserStream = transactionByUserIdStream.join(userTable, Pair::of);

        // Detect transactions which are over a user's single transaction limit
        transactionsWithUserStream.filter((key, transactionWithUser) -> transactionWithUser.getLeft().getAmount() >
                        transactionWithUser.getRight().getSingleTransactionLimit())
                .map((key, value) -> new KeyValue<>(value.getLeft().getId().toString(),
                        new FraudulentTransaction(value.getLeft(), FraudType.OVERSPEND)))
                .to(FRAUDULENT_TRANSACTIONS_TOPIC, Produced.with(null, FRAUDULENT_TRANSACTION_SERDE));

        // Detect transactions which are made at too high of a volume
        transactionsWithUserStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(timeWindow)))
                .aggregate(ArrayList<Transaction>::new, (aggKey, newValue, aggValue) ->
                        {
                            aggValue.add(newValue.getLeft());
                            return aggValue;
                        },
                        Materialized.with(Serdes.String(), new JsonSerde<>(ArrayList.class)))
                .toStream()
                .filter((key, list) -> list.size() > threshold)
                .flatMap(Transaction.transactionsToFraudulentTransactionsMapper())
                .to(FRAUDULENT_TRANSACTIONS_TOPIC, Produced.with(null, FRAUDULENT_TRANSACTION_SERDE));

        // Detect transactions which have a timestamp outside the grace period
        transactionStream
                .filter((key, value) -> value.getTimestamp().isAfter(LocalDateTime.now().plusSeconds(gracePeriod)) ||
                        value.getTimestamp().isBefore(LocalDateTime.now().minusSeconds(gracePeriod)))
                .map((key, value) -> new KeyValue<>(key, new FraudulentTransaction(value, FraudType.INVALID_TIME)))
                .to(FRAUDULENT_TRANSACTIONS_TOPIC, Produced.with(null, FRAUDULENT_TRANSACTION_SERDE));

        // Create a queryable state store for the number of fraudulent transactions by user ID
        streamsBuilder
                .stream(FRAUDULENT_TRANSACTIONS_TOPIC, Consumed.with(null, FRAUDULENT_TRANSACTION_SERDE))
                .map((key, transaction) -> new KeyValue<>(String.valueOf(transaction.getTransaction().getUserId()), transaction))
                .groupByKey()
                .count(Materialized.with(Serdes.String(), Serdes.Integer()).as(FRAUDULENT_TRANSACTION_COUNT_STORE));

    }
}
