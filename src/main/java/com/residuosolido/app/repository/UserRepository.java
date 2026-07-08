package com.residuosolido.app.repository;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import java.util.Optional;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndActive(Role role, boolean active);
}
