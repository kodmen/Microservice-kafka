package com.bedir.demoKafka.service;

import com.bedir.demoKafka.model.Kisi;
import com.bedir.demoKafka.repository.KisiRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KisiService {

    private final KisiRepository repository;

    public KisiService(KisiRepository repository) {
        this.repository = repository;
    }

    public List<Kisi> getAll(){
        return repository.findAll();
    }
}
