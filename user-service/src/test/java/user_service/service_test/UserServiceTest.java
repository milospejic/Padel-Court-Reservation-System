package user_service.service_test;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import user_service.dto.UserDto;
import user_service.implementation.UserServiceImplementation;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.InvalidRequestException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceRepository repo;

    @InjectMocks
    private UserServiceImplementation userService;

    private String validAdminAuth;
    private UserDto newUser;

    @BeforeEach
    void setUp() {
        // Create a fake Basic Auth header: "admin@uns.ac.rs:password"
        String auth = "admin@uns.ac.rs:password";
        validAdminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        newUser = new UserDto();
        newUser.setEmail("new@uns.ac.rs");
        newUser.setPassword("123");
        newUser.setRole("USER");
    }

    @Test
    void createUser_Success() {
        // 1. Define Behavior: When checking if user exists, return null (meaning user doesn't exist yet)
        when(repo.findByEmail(newUser.getEmail())).thenReturn(null);
        
        // 2. Mock saving: Return the model that would be saved
        UserModel savedModel = new UserModel(newUser.getEmail(), newUser.getPassword(), newUser.getRole());
        when(repo.save(any(UserModel.class))).thenReturn(savedModel);

        // 3. Execute
        ResponseEntity<?> response = userService.createUser(newUser, validAdminAuth);

        // 4. Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(UserModel.class));
    }

    @Test
    void createUser_Fail_AlreadyExists() {
        // 1. Define Behavior: User ALREADY exists
        when(repo.findByEmail(newUser.getEmail())).thenReturn(new UserModel());

        // 2. Execute & Verify Exception
        assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.createUser(newUser, validAdminAuth);
        });

        // 3. Ensure save was NEVER called
        verify(repo, times(0)).save(any(UserModel.class));
    }

    @Test
    void createUser_Fail_InvalidRole() {
        newUser.setRole("SUPER_ADMIN"); // Invalid Role

        assertThrows(InvalidRequestException.class, () -> {
            userService.createUser(newUser, validAdminAuth);
        });
    }
}