package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class IncorrectTimestampTransactor extends Transactor {

    private final Transactor delegate;

    public IncorrectTimestampTransactor(Transactor delegate) {
        super(delegate.user);
        this.delegate = delegate;
    }

    public List<Transaction> getTransactions() {
        if (delegate.shouldCommitFraud()) {
            int timeOffset = random.nextInt(21) - 10;
            return delegate.getTransactions().stream()
                    .map(t -> new Transaction(t.getUserId(), t.getDestinationId(), t.getAmount(),
                            t.getTimestamp().plusMinutes(timeOffset)))
                    .collect(Collectors.toList());
        } else {
            return delegate.getTransactions();
        }
    }
}
