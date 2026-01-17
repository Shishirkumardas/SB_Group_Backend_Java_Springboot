package org.example.sbgroup2.repositories;

import org.example.sbgroup2.models.Purchase;
import org.example.sbgroup2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
//    User findByEmail(String email);

}
