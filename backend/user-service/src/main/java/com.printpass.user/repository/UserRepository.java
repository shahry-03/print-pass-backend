package main.java.com.printpass.repository;

import main.java.com.printpass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository ke andar save(), findAll(), findById() pehle se mojood hote hain!

    // Naya Method: Email se user dhoondna
    Optional<User> findByEmail(String email);
}