package com.event.driven.order.enrichment.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class OrderEnrichmentStreamsApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderEnrichmentStreamsApplication.class, args);
    }
}
