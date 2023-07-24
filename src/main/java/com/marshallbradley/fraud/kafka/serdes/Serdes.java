package com.marshallbradley.fraud.kafka.serdes;

import com.marshallbradley.fraud.models.FraudulentTransaction;
import com.marshallbradley.fraud.models.Transaction;
import com.marshallbradley.fraud.models.User;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.kafka.support.serializer.JsonSerde;

public class Serdes {
    public static final Serde<User> USER_SERDE = new JsonSerde<>(User.class);
    public static final Serde<Transaction> TRANSACTION_SERDE = new JsonSerde<>(Transaction.class);
    public static final Serde<FraudulentTransaction> FRAUDULENT_TRANSACTION_SERDE = new JsonSerde<>(FraudulentTransaction.class);
}
