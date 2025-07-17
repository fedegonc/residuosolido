package com.residuosolido.app.repository;

import com.residuosolido.app.model.Phone;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
    List<Phone> findByActiveTrue();
    List<Phone> findByUsersId(Long userId);
    List<Phone> findByType(Phone.PhoneType type);
}
