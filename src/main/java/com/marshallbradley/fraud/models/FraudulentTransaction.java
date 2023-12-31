package com.marshallbradley.fraud.models;

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
