package com.bedir.demoKafka.controller;

import com.bedir.demoKafka.model.Kisi;
import com.bedir.demoKafka.service.KisiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kisi")
public class KisiController {

    private final KisiService service;

    public KisiController(KisiService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Kisi>> get(){
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }
}
