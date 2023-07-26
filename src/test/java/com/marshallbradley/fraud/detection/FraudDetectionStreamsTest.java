package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.models.FraudType;
import com.marshallbradley.fraud.models.FraudulentTransaction;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import kafka.utils.Json;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static com.marshallbradley.fraud.detection.FraudDetectionStreams.*;
import static com.marshallbradley.fraud.serdes.Serdes.*;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;

@SpringBootTest
@RunWith(SpringRunner.class)
class FraudDetectionStreamsTest {

    @Autowired
    FraudDetectionStreams fraudDetectionStreams;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, User> userTestInputTopic;
    private TestInputTopic<String, Transaction> transactionTestInputTopic;
    private TestOutputTopic<String, FraudulentTransaction> fraudulentTransactionTestOutputTopic;
    @Autowired
    private StreamsBuilder streamsBuilder;

    @BeforeEach
    void setUp() {
        Properties config = new Properties();
        config.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        config.put(TRUSTED_PACKAGES, "*");
        testDriver = new TopologyTestDriver(streamsBuilder.build(), config);
        transactionTestInputTopic = testDriver.createInputTopic(TRANSACTIONS_TOPIC, Serdes.String().serializer(), TRANSACTION_SERDE.serializer());
        fraudulentTransactionTestOutputTopic = testDriver.createOutputTopic(FRAUDULENT_TRANSACTIONS_TOPIC, Serdes.String().deserializer(), FRAUDULENT_TRANSACTION_SERDE.deserializer());
        userTestInputTopic = testDriver.createInputTopic(USERS_TOPIC, Serdes.String().serializer(), USER_SERDE.serializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void testNormalTransaction() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "John Doe", 500, "normal", 0);
        Transaction transaction = new Transaction(userId, UUID.randomUUID(), 200, LocalDateTime.now());

        // When
        userTestInputTopic.pipeInput(userId.toString(), user);
        transactionTestInputTopic.pipeInput(transaction.getId().toString(), transaction);

        // Then
        assertEquals(0, fraudulentTransactionTestOutputTopic.getQueueSize());
    }

    @Test
    void testOverspendTransaction() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "John Doe", 500, "overspend", 100);
        Transaction transaction = new Transaction(userId, UUID.randomUUID(), 1000, LocalDateTime.now());

        // When
        userTestInputTopic.pipeInput(userId.toString(), user);
        transactionTestInputTopic.pipeInput(transaction.getId().toString(), transaction);

        // Then
        assertEquals(1, fraudulentTransactionTestOutputTopic.getQueueSize());
        FraudulentTransaction fraudulentTransaction = fraudulentTransactionTestOutputTopic.readValue();
        assertEquals(fraudulentTransaction.getFraudType(), FraudType.OVERSPEND);
    }

    @Test
    void testHighVolumeTransactions() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "John Doe", 500, "high-volume", 100);

        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            transactions.add(new Transaction(userId, UUID.randomUUID(), 200, LocalDateTime.now()));
        }

        // When
        userTestInputTopic.pipeInput(userId.toString(), user);
        for (int i = 0; i < 10; i++) {
            Transaction transaction = transactions.get(i);
            transactionTestInputTopic.pipeInput(transaction.getId().toString(), transaction);
        }

        // Then
        FraudulentTransaction fraudulentTransaction = fraudulentTransactionTestOutputTopic.readValue();
        assertEquals(fraudulentTransaction.getFraudType(), FraudType.HIGH_VOLUME);
    }

    @Test
    void testIncorrectTimeTransaction() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "John Doe", 500, "incorrect-timestamp", 100);
        Transaction transaction = new Transaction(userId, UUID.randomUUID(), 200, LocalDateTime.now()
                .plusMinutes(10));

        // When
        userTestInputTopic.pipeInput(userId.toString(), user);
        transactionTestInputTopic.pipeInput(transaction.getId().toString(), transaction);

        // Then
        assertEquals(1, fraudulentTransactionTestOutputTopic.getQueueSize());
        FraudulentTransaction fraudulentTransaction = fraudulentTransactionTestOutputTopic.readValue();
        assertEquals(fraudulentTransaction.getFraudType(), FraudType.INCORRECT_TIME);
    }
}