package com.bedir.demoKafka.service;

import com.bedir.demoKafka.model.Kisi;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProduckerService {

    private static final String TOPIC_NAME = "bedir";

    private final KafkaTemplate<String, Kisi> kafkaTemplate;

    public KafkaProduckerService(KafkaTemplate<String, Kisi> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Kisi kisiSendEvent){
        //System.out.println(kisiSendEvent.toString());
        kafkaTemplate.send(TOPIC_NAME, UUID.randomUUID().toString(), kisiSendEvent);
    }
}
