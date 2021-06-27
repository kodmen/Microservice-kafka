package com.bedir.demoKafka.repository;

import com.bedir.demoKafka.model.Kisi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KisiRepository extends JpaRepository<Kisi , Long> {
}
