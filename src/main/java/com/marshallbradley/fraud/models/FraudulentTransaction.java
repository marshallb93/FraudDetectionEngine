package com.marshallbradley.fraud.models;

import com.marshallbradley.fraud.detection.models.FraudType;
import com.marshallbradley.fraud.models.Transaction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class FraudulentTransaction {
    Transaction transaction;
    FraudType fraudType;
}
