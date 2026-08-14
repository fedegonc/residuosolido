package com.residuosolido.app.repository;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.City;
import java.util.Optional;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndCity(Role role, City city);
    List<User> findByRoleAndCityAndActive(Role role, City city, boolean active);
}
