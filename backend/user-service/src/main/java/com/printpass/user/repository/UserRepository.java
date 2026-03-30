package com.printpass.user.repository;


import com.printpass.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // JpaRepository ke andar save(), findAll(), findById() pehle se mojood hote hain!

    // Naya Method: Auth Service Email se user dhundhega
    Optional<User> findByEmail(String email);

    // Email already exist karta hai? (Registration ke waqt check)
    boolean existsByEmail(String email);

}