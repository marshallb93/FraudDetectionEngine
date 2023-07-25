package com.marshallbradley.fraud.generation.transactors;

import com.marshallbradley.fraud.models.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactorFactoryTest {

    @Test
    public void testCreateTransactorForNormalUser() {
        // Given
        User user = new User(UUID.randomUUID(), "Joe Bloggs", 1000, "normal", 0);

        // When
        Transactor transactor = TransactorFactory.createTransactor(user);

        // Then
        assertNotNull(transactor);
        assertEquals(user, transactor.getUser());
    }

    @Test
    public void testCreateTransactorForHighVolumeUser() {
        // Given
        User user = new User(UUID.randomUUID(), "Joe Bloggs", 1000, "high-volume", 20);

        // When
        Transactor transactor = TransactorFactory.createTransactor(user);

        // Then
        assertNotNull(transactor);
        assertTrue(transactor instanceof HighVolumeTransactor);
        assertEquals(user, transactor.getUser());
    }

    @Test
    public void testCreateTransactorForIncorrectTimestampUser() {
        // Given
        User user = new User(UUID.randomUUID(), "Joe Bloggs", 1000, "incorrect-timestamp", 20);

        // When
        Transactor transactor = TransactorFactory.createTransactor(user);

        // Then
        assertNotNull(transactor);
        assertTrue(transactor instanceof IncorrectTimestampTransactor);
        assertEquals(user, transactor.getUser());
    }

    @Test
    public void testCreateTransactorForOverspendUser() {
        // Given
        User user = new User(UUID.randomUUID(), "Joe Bloggs", 1000, "overspend", 20);

        // When
        Transactor transactor = TransactorFactory.createTransactor(user);

        // Then
        assertNotNull(transactor);
        assertTrue(transactor instanceof OverspendTransactor);
        assertEquals(user, transactor.getUser());
    }

    @Test
    public void testCreateTransactorForUnknownUserType() {
        User user = new User(UUID.randomUUID(), "Joe Bloggs", 1000, "unknown", 0);

        assertThrows(IllegalArgumentException.class, () -> {
            TransactorFactory.createTransactor(user);
        });
    }
}