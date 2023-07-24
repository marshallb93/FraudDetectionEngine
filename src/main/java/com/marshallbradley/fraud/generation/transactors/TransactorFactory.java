package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.User;

public class TransactorFactory {

    public static Transactor createTransactor(User user) {
        Transactor transactor = new Transactor(user);
        if (user.getType().equals("normal")) {
            return transactor;
        }
        if (user.getType().equals("high-volume")) {
            return new HighVolumeTransactor(transactor);
        }
        if (user.getType().equals("incorrect-timestamp")) {
            return new IncorrectTimestampTransactor(transactor);
        }
        if (user.getType().equals("overspend")) {
            return new OverspendTransactor(transactor);
        }
        return null;
    }
}
