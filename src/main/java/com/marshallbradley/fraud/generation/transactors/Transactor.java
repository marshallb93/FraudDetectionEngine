package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
public class Transactor {

    protected User user;

    public Transactor(User user) {
        this.user = user;
    }

    public List<Transaction> getTransactions() {

        Random random = new Random();
        int amount = random.nextInt(user.getSingleTransactionLimit() + 1);
        Transaction transaction = new Transaction(user.getId(), UUID.randomUUID(), amount, LocalDateTime.now());

        return List.of(transaction);
    }
}
