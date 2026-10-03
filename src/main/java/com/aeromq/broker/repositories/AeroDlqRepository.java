package com.aeromq.broker.repositories;

import com.aeromq.broker.model.AeroDlq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AeroDlqRepository extends JpaRepository<AeroDlq, Long> {

    List<AeroDlq> findTop20ByOrderByFailedAtDesc();
}
