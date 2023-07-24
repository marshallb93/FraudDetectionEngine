package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.models.Transaction;
import lombok.Value;

@Value
public class FraudulentTransaction {
    Transaction transaction;
    FraudType fraudType;
}
