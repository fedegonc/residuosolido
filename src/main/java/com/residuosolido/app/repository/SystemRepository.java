package com.residuosolido.app.repository;

import com.residuosolido.app.model.System;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemRepository extends JpaRepository<System, Long> {
    System findByKey(String key);
}
