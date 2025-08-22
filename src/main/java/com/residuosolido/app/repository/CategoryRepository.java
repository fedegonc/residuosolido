package com.residuosolido.app.repository;

import com.residuosolido.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    List<Category> findAllByOrderByDisplayOrderAsc();
}
