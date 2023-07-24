package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.Transaction;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HighVolumeTransactor extends Transactor {


    private final Transactor delegate;

    public HighVolumeTransactor(Transactor delegate) {
        super(delegate.user);
        this.delegate = delegate;
    }

    public List<Transaction> getTransactions() {
        if (delegate.shouldCommitFraud()) {
            int transactionCount = random.nextInt(20) + 1;
            // This transactor sends between 1 and 20 transactions in a single time interval
            return IntStream.range(0, transactionCount)
                    .mapToObj(i -> {
                        Transaction transaction = delegate.getTransactions().get(0);
                        return new Transaction(
                                transaction.getId(),
                                transaction.getDestinationId(),
                                transaction.getAmount(),
                                transaction.getTimestamp()
                        );
                    })
                    .collect(Collectors.toList());
        } else {
            return delegate.getTransactions();
        }
    }
}
