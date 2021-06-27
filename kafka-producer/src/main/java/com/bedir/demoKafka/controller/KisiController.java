package com.bedir.demoKafka.controller;

import com.bedir.demoKafka.model.Kisi;
import com.bedir.demoKafka.service.KafkaProduckerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kisi")
public class KisiController {

    private final KafkaProduckerService service;

    public KisiController(KafkaProduckerService service) {
        this.service = service;
    }

    @GetMapping(value = "/{isim}/{soyisim}")
    public ResponseEntity sendKisiToKafkaTopic(@PathVariable("isim") String isim, @PathVariable("soyisim") String soyisim){
        Kisi kisi = new Kisi(null,isim,soyisim);
        service.send(kisi);
       return new ResponseEntity(HttpStatus.OK);
    }
}
