blog yazısının kod örneği: [http://www.hanrideb.com/2021/06/27/198/](http://www.hanrideb.com/2021/06/27/198/)

apache kafka bir mesajlaşma sistemidir. Linked tarafından geliştirilen açık kaynak bir framework'tür. 

birçok kullanım alanı vardır ama ben bu yazıda microservis mimarisinde servisler arasında iletişimi(mesajlaşma) klavyem yettiğince anlatmaya çalışıcağım.

normalde microservislera arasında mesajlaşmada bir çok sorun vardır protokoller(TCP, HTTP, FTP, JDBC, REST vb), veri formatları(JSON, CSV, XML, Avro, Thrift vb) ve servislere entegrasyonu. bu sorunlara çözüm olarak apache kafka bize çözüm oluyor.

öncelikle apache'nin terminalojisini kısaca değiniyim
1. Producer: kafka'ya mesaj yazmayı sağlayan bileşen
2. Consumer: Kafka'dan mesajı okuyan bileşen
3. Consumer Gurup: içinde Consumer olan grup. Her Consumer'in grubu olmak zorunda
4. Topic: Veriyi depoladığımız alan.
5. Partition: Topiclerin içindeki Verileri depolayabiliceğimz Alanların ismi default olarak oluşturunca en az birtane olması lazım
6. Kafka Brokers: içinde Topic'lerin bulunduğu bileşen
7. kafka Cluster: içinde brokerların olduğu bileşen

öncelikle kafkanın kurulumundan başlayalım her işletim sistemine özgü kurulumu var ama ben docker üstünde ayağa kadırıcam daha hızlı ve işletim sistemi yüzüden hata alma olasığım daha az.
```yml
version: "3.5"

services:
  zookeeper:
    image: "docker.io/bitnami/zookeeper:3"
    ports:
      - "2181:2181"
    volumes:
      - "zookeeper_data:/bitnami"
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
  kafka:
    image: "docker.io/bitnami/kafka:2-debian-10"
    ports:
      - "9092:9092"
    expose:
      - "9093"
    volumes:
      - "kafka_data:/bitnami"
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_ADVERTISED_LISTENERS=INSIDE://kafka:9093,OUTSIDE://localhost:9092
      - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      - KAFKA_LISTENERS=INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
      - KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
    depends_on:
      - zookeeper

  kafka-ui:
    image: provectuslabs/kafka-ui
    container_name: kafka-ui
    ports:
      - "9090:8080"
    restart: always
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9093
      - KAFKA_CLUSTERS_0_ZOOKEEPER=localhost:2181

volumes:
  zookeeper_data:
    driver: local
  kafka_data:
    driver: local
```

ben bi tane küçük bi microservis haberleşmesi örneği yapacağım konuyu onun üstünde anlatmaya çalışacağım. İki tane servis oluşturucam birisinde publisher olucak yani veriyi gönderen içinde producer olucak. Diğer servis Subscriber olucak yani gönderilen mesajı okuyan servis içinde consumer olucak. İlk servisi yazmakla başlayalım biraz elimizi kirletelim.

```xml
	<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka</artifactId>
		</dependency>
```
pom.xml'e kafkanın dependency'sini ekleyerek başlayalım


```properties
spring.kafka.producer.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```
öncelikle uygulamanın configurasyonunu yapmakla başlıyorum bu üc özelliği mutlaka tanımlamalıyız kafka bunlarsız çalışmayacaktır. 

```java
@Configuration
@EnableKafka
public class KafkaConfiguration {

    private final String bootstrapServers = "localhost:9092";

    @Bean
    public ProducerFactory<String, Kisi> producerFactory(){
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Kisi> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic bedirTopic(){
        return TopicBuilder.name("bedir")
                .partitions(1)
                .replicas(1)
                .compact().build();
    }
}
```

Buradada producer'ımızı oluşturuyoruz kod üstünden. Spring boot'da @Bean anatasyonuyla KafkaTemplate oluşturuyoruz bunu projemizini içinde sadece çağırarak kolayca kullanabilceğiz. Birde orda Yeni birtane Topic oluşturuyoz, topic'i oluşturuken partition sayısını böyle verebiliriz.

```java
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
```

göndereceğimiz mesajın içeriğini böyle hazırladım config dosyasındada Kisi sınıfını görmüşsünüzdür. Bu nesneyi kafka ile göndereceğiz.

```java
@Service
public class KafkaProduckerService {

    private static final String TOPIC_NAME = "bedir";

    private final KafkaTemplate<String, Kisi> kafkaTemplate;

    public KafkaProduckerService(KafkaTemplate<String, Kisi> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Kisi kisiSendEvent){
        kafkaTemplate.send(TOPIC_NAME, UUID.randomUUID().toString(), kisiSendEvent);//gönderilecek topic - gönderilen mesajın id'si - mesajın kendisi
    }
}
```

burdada gönderme işleminin yapıldığı servis KafkaTemplate nesnesini çağırmamız lazım config'de oluşturmuştuk. Sonra çok kolay bir yekilde gönderebiliyoruz.

```java
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
```

burdada aldığımız değeri apache kafka ile topic'e gönderiyoruz.

Şimdi ikinci servisi oluşturmaya başlayabiliriz.

yine application servis ile başlayacağım
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=group-id
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

bu projede örnek olsun diye h2 database kullandım üst taraftaki özellikler onunla alakalı bizim ilgilenmemiz gereken yer daha çok alt kısım olucak burda producerden farklı olarak group-id var burasıda her consumer'ın mütlaka bir grubun içinde olması lazım. hemde dikkat ederseniz burda key-deserializer var çünkü aldığımız dereği deserialize etmemiz lazım.

```java
@Configuration
public class KafkaConfiguration {

    private final String bootstrapServers = "localhost:9092";
    private final String groupId = "group-id";

    @Bean
    public ConsumerFactory<String, Kisi> consumerFactory(){
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,Kisi.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Kisi> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, Kisi> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

burda Consumer'imizi oluşturuyoruz.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Entity
public class Kisi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String isim;
    private String soyisim;
}
```

burda yine Kişi sınıfımız var çünkü aldığımız nesneyi Kişi sınıfına çeviriyoruz config'in içinde.

```java
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
```

burasıda bizim topic'imizi dinlediğimiz yer. @KafkaListerner anatasyonunu koyarak kolayca topic'leri dinleyebiliriz anatasyonun içine daha başka özellikler de ekleye biliriz hangi partition'u dinleyeceğimiz gibi. Ben burda aldığımız Kisi nesnesini h2 veritabanına kayıt ediyoruz.

```java
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
```

burdada kayıt ettiğimiz kişileri yazdırıyoruz. kisiServis ile kisiRepository sınıflarını yazmadım buraya daha kalabalık olmasın diye eğer isterseniz github'dan projenin kalanına ulaşabilirsiniz: [https://github.com/kodmen/Microservice-kafka](https://github.com/kodmen/Microservice-kafka)

## Kaynakça
- [Kafka: The Definitive Guide](https://assets.confluent.io/m/1b509accf21490f0/original/20170707-EB-Confluent_Kafka_Definitive-Guide_Complete.pdf?mkt_tok=NTgyLVFIWC0yNjIAAAF944HczCD8cx08o0ilu69SyGfYNTqDeZ-UgJbqQXCPeaRvtHXniSmC8mMq4Y7HXNKjZgBmWGMMoaGM-t_g3DpV0Dq0nT01832IpLGni_GfiXY0)
- [haydikodlayalım youtube kanalı](https://www.youtube.com/watch?v=dLDDCYAGNpM&list=PLd0jsEi3hUAfg1-tqxFvDA9q-kpZ4q4uE&index=21)
- [folkdev twitch kanalı](https://www.twitch.tv/folksdev)
