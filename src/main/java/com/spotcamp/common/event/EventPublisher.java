package com.spotcamp.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Event publisher that handles both local and distributed events
 * Uses Spring ApplicationEventPublisher for local events
 * Uses Kafka for distributed events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    /**
     * Publishes event locally within the same service
     */
    public void publishLocal(DomainEvent event) {
        log.debug("Publishing local event: {}", event.getEventType());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publishes event to Kafka for cross-service communication
     */
    public void publishExternal(DomainEvent event) {
        log.debug("Publishing external event: {} to topic: {}", 
                 event.getEventType(), event.getEventType());
        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.debug("Kafka disabled; skipping external publish for event {}", event.getEventType());
            return;
        }

        kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Event published successfully: {}", event.getEventType());
                    } else {
                        log.error("Failed to publish event: {}", event.getEventType(), ex);
                    }
                });
    }

    /**
     * Publishes event both locally and externally
     */
    public void publishAll(DomainEvent event) {
        publishLocal(event);
        publishExternal(event);
    }
}
