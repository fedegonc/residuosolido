package com.residuosolido.app.repository;

import com.residuosolido.app.model.CollectionRequest;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Long> {
   }
