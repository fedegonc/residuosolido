package com.residuosolido.app.repository;

import com.residuosolido.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    List<Category> findAllByOrderByDisplayOrderAsc();
}
