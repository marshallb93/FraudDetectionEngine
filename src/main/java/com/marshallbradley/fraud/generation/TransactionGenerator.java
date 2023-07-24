package com.marshallbradley.fraud.generation;

import com.marshallbradley.fraud.generation.transactors.HighVolumeTransactor;
import com.marshallbradley.fraud.generation.transactors.IncorrectTimestampTransactor;
import com.marshallbradley.fraud.generation.transactors.OverspendTransactor;
import com.marshallbradley.fraud.generation.transactors.Transactor;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
public class TransactionGenerator {

    private static final String TRANSACTIONS_TOPIC = "transactions";
    private static final String USERS_TOPIC = "users";
    private final List<Transactor> transactors = List.of(
            new Transactor(new User(UUID.randomUUID(), "Larry Brown", 1000)),
            new HighVolumeTransactor(new Transactor(new User(UUID.randomUUID(), "High-Roller Hugh", 10000))),
            new OverspendTransactor(new Transactor(new User(UUID.randomUUID(), "Sneaky Pete", 500))),
            new IncorrectTimestampTransactor(new Transactor(new User(UUID.randomUUID(), "Schemin' Susan", 1000)))
    );

    @Autowired
    private Producer<String, Object> producer;

    @PostConstruct
    private void initializeUsers() {
        for (Transactor transactor : transactors) {
            ProducerRecord<String, Object> record = new ProducerRecord<>(USERS_TOPIC, transactor.getUser().getId().toString(), transactor.getUser());
            producer.send(record);
        }

    }

    @Scheduled(fixedRate = 1000)
    public void postTransactions() {
        for (Transactor transactor : transactors) {
            List<Transaction> transactions = transactor.getTransactions();
            for (Transaction transaction : transactions) {
                ProducerRecord<String, Object> record = new ProducerRecord<>(TRANSACTIONS_TOPIC, transaction.getUserId().toString(), transaction);
                producer.send(record);
            }
        }
    }
}
