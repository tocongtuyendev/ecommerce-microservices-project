package com.yourcompany.ecommerce.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> publish(String topic, String key, Object payloadJson) {
        try {
            String json = mapper.writeValueAsString(payloadJson);
            ListenableFuture<?> fut = kafkaTemplate.send(topic, key, json);
            CompletableFuture<Object> cf = new CompletableFuture<>();
            fut.addCallback(new ListenableFutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    cf.complete(result);
                }

                @Override
                public void onFailure(Throwable ex) {
                    cf.completeExceptionally(ex);
                }
            });
            return Mono.fromFuture(cf).then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
