package com.printpass.user.service.impl;

import com.printpass.user.dto.response.InternalUserResponse;
import com.printpass.user.entity.User;
import com.printpass.user.enums.Role;
import com.printpass.user.exception.DuplicateResourceException;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.repository.UserRepository;
import com.printpass.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    // Dependencies hamesha 'private final' honi chahiye
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String email, String password, Role role) {
        // 1. Pehle check karein ke kahin email pehle se toh nahi?
        if(userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists!" + email);
        }
        // 2. User Object banana
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))  //BCrycpt Hash
                .isActive(true)   // Default active status
                .role(role)
                .build();

        // 3. Database mein save karein aur RETURN karein
        return userRepository.save(user);
    }

    @Override
    public InternalUserResponse getUserByEmail(String email) {
        // 1. Database se user dhoondo, agar na mile toh Exception throw karo
        User user = userRepository.findByEmail(email)
                //  PEHLE (Old): throw new RuntimeException(...)
                //  AB (New): throw new ResourceNotFoundException(...)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "+ email));

        // 2. User Entity ko InternalUserResponse DTO mein convert karo (taake Auth Service ko password verify karne ke liye mil sake)
        return InternalUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())   // ← Hashed — Auth Service verify karega
                .role(user.getRole())
                .isActive(user.isActive())
                .build();
    }

    @Override
    public void toggleUserStatus(UUID userId) {
        // 1. Database se ID ke zariye user nikalo, agar na mile toh Exception throw karo
        User user = userRepository.findById(userId)
                // PEHLE (Old): throw new RuntimeException(...)
                // AB (New): throw new ResourceNotFoundException(...)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: "+ userId));
        // 2. Jo bhi current status hai (true/false), usko ulat (reverse) kar do
        user.setActive(!user.isActive()); // toggle

        // 3. Updated User ko wapas database me save kardo
        userRepository.save(user);
    }
}
