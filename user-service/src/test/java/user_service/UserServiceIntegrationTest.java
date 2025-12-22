package user_service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import user_service.dto.UserDto;
import user_service.repository.UserServiceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserServiceRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    private String validAdminAuth;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        String auth = "admin@uns.ac.rs:password";
        validAdminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Test
    void createUser_Flow() throws Exception {
        UserDto newUser = new UserDto("test@uns.ac.rs", "123", "USER");

        mockMvc.perform(post("/user")
                .header("Authorization", validAdminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("test@uns.ac.rs")));

        mockMvc.perform(get("/user/email/test@uns.ac.rs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void createUser_Forbidden_AsAdmin() throws Exception {
        repo.save(new user_service.model.UserModel("admin@uns.ac.rs", "password", "ADMIN"));

        UserDto newAdmin = new UserDto("otheradmin@uns.ac.rs", "123", "ADMIN");

        mockMvc.perform(post("/user")
                .header("Authorization", validAdminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isForbidden());
    }
}