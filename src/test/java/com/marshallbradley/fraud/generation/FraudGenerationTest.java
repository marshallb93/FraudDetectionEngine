package com.marshallbradley.fraud.generation;

import com.marshallbradley.fraud.generation.transactors.Transactor;
import com.marshallbradley.fraud.generation.transactors.TransactorFactory;
import com.marshallbradley.fraud.models.User;
import com.marshallbradley.fraud.models.UserConfiguration;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FraudGenerationTest {

    @MockBean
    private UserConfiguration userConfiguration;

    @MockBean
    private Producer<String, Object> producer;

    @Autowired
    private TransactionGenerator transactionGenerator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionGenerator = new TransactionGenerator();
    }

    @Test
    public void testInitializeTransactors() {
        // Given
        List<User> users = new ArrayList<>();
        User user1 = new User(UUID.randomUUID(), "Joe Bloggs", 100, "normal", 0);
        User user2 = new User(UUID.randomUUID(), "Mary Berry", 100, "normal", 0);
        users.add(user1);
        users.add(user2);
        when(userConfiguration.getProfiles()).thenReturn(users);

        // When
        Transactor transactor1 = mock(Transactor.class);
        Transactor transactor2 = mock(Transactor.class); // Simulating a null transactor
        when(TransactorFactory.createTransactor(user1)).thenReturn(transactor1);
        when(TransactorFactory.createTransactor(user2)).thenReturn(transactor2);

        // Then
        verify(producer, times(2)).send(any(ProducerRecord.class));
        verify(producer).send(new ProducerRecord<>("users", "user1", user1));
        verifyNoMoreInteractions(producer);
    }

    @Test
    public void testPostTransactions() {
        // Given
        Transactor transactor1 = mock(Transactor.class);
        Transactor transactor2 = mock(Transactor.class);
        List<Transactor> transactors = new ArrayList<>();
        transactors.add(transactor1);
        transactors.add(transactor2);
        when(transactor1.getTransactions()).thenReturn(new ArrayList<>());
        when(transactor2.getTransactions()).thenReturn(new ArrayList<>());

        // When
        transactionGenerator.postTransactions();

        // Then
        verify(producer, times(4)).send(any(ProducerRecord.class));
        verifyNoMoreInteractions(producer);
    }
}
