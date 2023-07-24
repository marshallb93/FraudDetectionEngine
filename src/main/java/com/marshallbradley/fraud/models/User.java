package com.marshallbradley.fraud.models;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class User {
    UUID id;
    String name;
    Integer limit;
    String type;
    Integer naughtiness;

    public void setId(String id) {
        this.id = UUID.fromString(id);
    }
}
