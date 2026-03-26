package main.java.com.printpass.service;

import main.java.com.printpass.dto.AuthRequest;  //new import for login request
import main.java.com.printpass.dto.UserRequest;
import main.java.com.printpass.dto.UserResponse;
import main.java.com.printpass.entity.User;
import main.java.com.printpass.repository.UserRepository;
import main.java.com.printpass.security.JwtService;  //new import for JWT token generation
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;  // <--- JWT Token Banane Wala Service Inject kiya
    private final PasswordEncoder passwordEncoder; // <--- Hashing Tool Inject kiya

    public UserResponse createUser(UserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // <--- Password Encrypt ho raha hai!
                .role(request.getRole())
                .walletBalance(BigDecimal.ZERO)
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    // Naya Login Method
    public String login(AuthRequest request) {
        // 1. Database mein user ka email dhoondein
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: User is email se mojood nahi!"));

        // 2. Password match karein (Jo password Postman se aaya vs jo Database mein hai)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Error: Password ghalat hai!");
        }

        // 3. Agar sab theek hai, toh Token (ID Card) generate karein
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    // Database se saare users la kar unhein Response DTO mein convert karna
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToUserResponse).toList();
    }

    //method: get user by its id Ek
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return mapToUserResponse(user);
    }

    // Ek chota sa helper method jo User Entity ko UserResponse mein badalta hai
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .walletBalance(user.getWalletBalance())
                .build();
    }
}