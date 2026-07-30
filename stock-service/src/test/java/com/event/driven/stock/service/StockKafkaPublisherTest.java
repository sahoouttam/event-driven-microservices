package com.event.driven.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.stock.service.entity.OutboxEvent;
import com.event.driven.stock.service.enums.EventType;
import com.event.driven.stock.service.kafka.StockKafkaPubisher;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class StockKafkaPublisherTest {
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private StockKafkaPubisher stockKafkaPublisher;

    @Test
    void shouldPublishToInventoryTopic() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-123")
                        .eventType(EventType.STOCK_RESERVATION_CREATED)
                        .aggregateType(AggregateType.INVENTORY)
                        .aggregateId("10")
                        .payload("{\"sku\":\"SKU-001\"}")
                        .eventStatus(EventStatus.PENDING)
                        .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"envelope\":\"data\"}");
        
        stockKafkaPublisher.publish(outboxEvent);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(1)).send(
                topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertEquals(KafkaTopics.INVENTORY_EVENTS, topicCaptor.getValue());
        assertEquals("10", keyCaptor.getValue());
        assertTrue(valueCaptor.getValue().contains("envelope"));
    }

    @Test
    void shouldPublishToProductTopic() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-1234")
                        .eventType(EventType.PRODUCT_CREATED)
                        .aggregateType(AggregateType.PRODUCT)
                        .aggregateId("11")
                        .payload("{}")
                        .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        stockKafkaPublisher.publish(outboxEvent);

        verify(kafkaTemplate).send(eq(KafkaTopics.PRODUCT_EVENTS), eq("11"), anyString());
    }

    @Test
    void shouldThrowExceptionForUnknownAggregateType() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .aggregateType(AggregateType.ORDER)
                        .build();
        
        assertThrows(IllegalStateException.class, () -> {
            stockKafkaPublisher.publish(outboxEvent);
        });

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldBuildEventEnvelopeCorrectly() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-1235")
                        .eventType(EventType.STOCK_RESERVATION_FAILED)
                        .aggregateType(AggregateType.INVENTORY)
                        .aggregateId("12")
                        .payload("{\"sku\":\"SKU-001\"}")
                        .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"mock\":\"envelope\"}");
        
        stockKafkaPublisher.publish(outboxEvent);

        verify(kafkaTemplate).send(eq(KafkaTopics.INVENTORY_EVENTS), 
                                    eq("12"), eq("{\"mock\":\"envelope\"}"));
    }

}
