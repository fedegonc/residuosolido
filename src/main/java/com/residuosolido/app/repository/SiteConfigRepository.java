package com.residuosolido.app.repository;

import com.residuosolido.app.model.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {
    Optional<SiteConfig> findByConfigKey(String configKey);
}
