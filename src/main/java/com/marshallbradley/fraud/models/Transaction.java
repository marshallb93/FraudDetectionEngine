package com.marshallbradley.fraud.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
