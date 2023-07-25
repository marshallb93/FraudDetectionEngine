package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.Transaction;

import java.util.List;
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
            return IntStream.range(0, 51)
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
