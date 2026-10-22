package com.printpass.user.security;

import com.printpass.user.repository.PrintShopProfileRepository;
import com.printpass.user.repository.UserRepository;
import com.printpass.user.service.EmailService;
import com.printpass.user.service.OtpService;
import com.printpass.user.service.PrintShopService;
import com.printpass.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    // Mocks for dependencies of Controllers
    @MockBean
    private PrintShopService printShopService;
    @MockBean
    private UserService userService;
    @MockBean
    private OtpService otpService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private PrintShopProfileRepository printShopProfileRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    void publicEndpoint_ShouldBeAccessibleWithoutAuth() throws Exception {
        // Hum khali JSON bhej rahe hain, toh validation fail hogi (400)
        mockMvc.perform(post("/api/v1/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }

    @Test
    void protectedEndpoint_ShouldReturn403WithoutToken() throws Exception {
        // Token nahi hai, toh 403 aayega
        mockMvc.perform(get("/api/v1/admin/shops"))
                .andExpect(status().isForbidden()); 
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_ShouldBeAccessible_WithAdminRole() throws Exception {
        // Role sahi hai, controller chalega aur 200 dega (kyunki mock service khali list dega)
        mockMvc.perform(get("/api/v1/admin/shops"))
                .andExpect(status().isOk()); 
    }

    @Test
    @WithMockUser(roles = "PRINT_SHOP")
    void adminEndpoint_ShouldReturn403_WithWrongRole() throws Exception {
        // Role ghalat hai, toh 403 aayega
        mockMvc.perform(get("/api/v1/admin/shops"))
                .andExpect(status().isForbidden());
    }
}