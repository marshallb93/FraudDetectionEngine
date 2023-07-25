package com.marshallbradley.fraud.generation;

import com.marshallbradley.fraud.generation.transactors.Transactor;
import com.marshallbradley.fraud.generation.transactors.TransactorFactory;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import com.marshallbradley.fraud.models.UserConfiguration;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionGenerator {

    private static final String TRANSACTIONS_TOPIC = "transactions";
    private static final String USERS_TOPIC = "users";
    private final List<Transactor> transactors = new ArrayList<>();
    @Autowired
    private UserConfiguration userConfiguration;
    @Autowired
    private Producer<String, Object> producer;

    @PostConstruct
    private void initializeTransactors() {
        List<User> users = userConfiguration.getProfiles();
        for (User user : users) {
            Transactor transactor = TransactorFactory.createTransactor(user);
            if (transactor != null) {
                transactors.add(transactor);
                ProducerRecord<String, Object> record = new ProducerRecord<>(USERS_TOPIC, transactor.getUser().getId().toString(), transactor.getUser());
                producer.send(record);
            }
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
