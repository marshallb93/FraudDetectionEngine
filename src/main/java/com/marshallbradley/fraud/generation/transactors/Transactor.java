package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
public class Transactor {

    protected User user;

    protected Random random = new Random();

    public Transactor(User user) {
        this.user = user;
    }

    public List<Transaction> getTransactions() {
        int amount = random.nextInt(user.getLimit() + 1);
        Transaction transaction = new Transaction(user.getId(), UUID.randomUUID(), amount, LocalDateTime.now());
        return List.of(transaction);
    }

    protected Boolean shouldCommitFraud() {
        return random.nextInt(101) > user.getNaughtiness();
    }
}
