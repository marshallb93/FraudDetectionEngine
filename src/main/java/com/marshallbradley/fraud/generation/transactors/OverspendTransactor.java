package com.marshallbradley.fraud.generation.transactors;
import com.marshallbradley.fraud.models.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class OverspendTransactor extends Transactor {

    private final Transactor delegate;

    public OverspendTransactor(Transactor delegate) {
        super(delegate.user);
        this.delegate = delegate;
    }

    public List<Transaction> getTransactions() {
        return delegate.getTransactions().stream()
                .map(t -> new Transaction(t.getUserId(), t.getDestinationId(), t.getAmount() * 2, t.getTimestamp()))
                .collect(Collectors.toList());
    }
}
