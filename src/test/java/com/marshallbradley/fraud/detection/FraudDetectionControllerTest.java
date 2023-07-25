package com.marshallbradley.fraud.detection;

import com.marshallbradley.fraud.models.Response;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@WebMvcTest
@RunWith(SpringRunner.class)
public class FraudDetectionControllerTest {

    @Autowired
    private FraudDetectionController fraudDetectionController;

    @MockBean
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Mock
    private KafkaStreams kafkaStreams;

    @Mock
    private ReadOnlyKeyValueStore<Object, Object> fraudulentTransactions;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetFraudulentTransactionsWithExistingUserId() {
        // Given
        String userId = UUID.randomUUID().toString();
        long transactionCount = 5L;
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                FraudDetectionStreams.FRAUDULENT_TRANSACTION_COUNT_STORE,
                QueryableStoreTypes.keyValueStore())))
                .thenReturn(fraudulentTransactions);
        when(fraudulentTransactions.get(userId)).thenReturn(transactionCount);

        // When
        ResponseEntity<Response> responseEntity = fraudDetectionController.getFraudulentTransactions(userId);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(UUID.fromString(userId), response.getUserId());
        assertEquals(transactionCount, response.getCount());
    }

    @Test
    public void testGetFraudulentTransactionsWithNonExistingUserId() {
        // Given
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        String userId = UUID.randomUUID().toString();
        when(kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                FraudDetectionStreams.FRAUDULENT_TRANSACTION_COUNT_STORE,
                QueryableStoreTypes.keyValueStore())))
                .thenReturn(fraudulentTransactions);
        when(fraudulentTransactions.get(userId)).thenReturn(null);

        // When
        ResponseEntity<Response> responseEntity = fraudDetectionController.getFraudulentTransactions(userId);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(UUID.fromString(userId), response.getUserId());
        assertEquals(0L, response.getCount());
    }
}