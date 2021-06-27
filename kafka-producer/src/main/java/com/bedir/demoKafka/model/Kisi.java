package com.bedir.demoKafka.model;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class Kisi {

    private Long id;
    private String isim;
    private String soyisim;


}
