package com.residuosolido.app.repository;

import com.residuosolido.app.model.HeroImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeroImageRepository extends JpaRepository<HeroImage, Long> {
    Optional<HeroImage> findByIsActiveTrue();
    List<HeroImage> findAllByOrderByUploadDateDesc();
}
