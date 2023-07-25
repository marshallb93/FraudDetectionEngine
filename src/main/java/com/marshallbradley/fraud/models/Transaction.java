package com.marshallbradley.fraud.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Transaction {

    UUID id;
    UUID userId;
    UUID destinationId;
    Integer amount;
    LocalDateTime timestamp;

    public Transaction(UUID userId, UUID destinationId, Integer amount, LocalDateTime timestamp) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.destinationId = destinationId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
