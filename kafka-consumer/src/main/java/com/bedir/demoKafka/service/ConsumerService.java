package com.bedir.demoKafka.service;

import com.bedir.demoKafka.model.Kisi;
import com.bedir.demoKafka.repository.KisiRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private final KisiRepository repository;

    public ConsumerService(KisiRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = "bedir",
            groupId = "group-id"
    )
    public void consume(@Payload Kisi kisi){
        repository.save(kisi);
    }
}
