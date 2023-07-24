package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class FraudDetectionService {


    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
//        transactionStream
//                .groupByKey()
//                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
//                .count()
//                .toStream()
//                .filter((key, count) -> count >= transactionCountThreshold)
//                .to("fraudulent-transactions");


        // for each transaction in the stream
        // join the transaction with the user from the user table
        //

        Properties settings = new Properties();
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());

        Serde<User> userSerde = new JsonSerde<>(User.class);
        Serde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
        Serde<FraudulentTransaction> fraudulentTransactionSerde  = new JsonSerde<>(FraudulentTransaction.class);

        KStream<String, Transaction> transactionStream = streamsBuilder
                .stream("transactions", Consumed.with(null, transactionSerde));

        KTable<String, User> userTable = streamsBuilder.table("users", Consumed.with(null, userSerde));

        KStream<String, Transaction> transactionByUserIdStream = transactionStream
                .map((key, transaction) -> new KeyValue<>(String.valueOf(transaction.getUserId()), transaction));

        transactionByUserIdStream.join(userTable, Pair::of)
                .filter((key, transactionWithUser) ->
                        transactionWithUser.getLeft().getAmount() >
                                transactionWithUser.getRight().getSingleTransactionLimit())
                .map((key, value) -> new KeyValue<>(value.getLeft().getId().toString(),
                        new FraudulentTransaction(value.getLeft(), FraudType.OVERSPEND)))
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
