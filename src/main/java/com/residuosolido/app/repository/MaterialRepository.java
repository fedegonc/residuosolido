package com.residuosolido.app.repository;

import com.residuosolido.app.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByActiveTrue();
    Material findByName(String name);

    @Modifying
    @Query(value = "DELETE FROM request_materials WHERE material_id = :materialId", nativeQuery = true)
    void deleteMaterialAssociations(@Param("materialId") Long materialId);

    @Modifying
    @Query(value = "DELETE FROM user_materials WHERE material_id = :materialId", nativeQuery = true)
    void deleteMaterialUserAssociations(@Param("materialId") Long materialId);
}
