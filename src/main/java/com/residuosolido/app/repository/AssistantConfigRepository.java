package com.residuosolido.app.repository;

import com.residuosolido.app.model.AssistantConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssistantConfigRepository extends JpaRepository<AssistantConfig, Long> {
    Optional<AssistantConfig> findFirstByIsActiveTrueOrderByUpdatedAtDesc();
}
