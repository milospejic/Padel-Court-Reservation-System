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
import util.exceptions.NoDataFoundException;

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
        String auth = "admin@uns.ac.rs:password";
        validAdminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        newUser = new UserDto();
        newUser.setEmail("new@uns.ac.rs");
        newUser.setPassword("123");
        newUser.setRole("USER");
    }

    @Test
    void createUser_Success() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(null);
        
        UserModel savedModel = new UserModel(newUser.getEmail(), newUser.getPassword(), newUser.getRole());
        when(repo.save(any(UserModel.class))).thenReturn(savedModel);

        ResponseEntity<?> response = userService.createUser(newUser, validAdminAuth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(UserModel.class));
    }

    @Test
    void createUser_Fail_AlreadyExists() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(new UserModel());

        assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.createUser(newUser, validAdminAuth);
        });

        verify(repo, times(0)).save(any(UserModel.class));
    }

    @Test
    void createUser_Fail_InvalidRole() {
        newUser.setRole("SUPER_ADMIN");

        assertThrows(InvalidRequestException.class, () -> {
            userService.createUser(newUser, validAdminAuth);
        });
    }
    
    @Test
    void updateUser_Success() {
        UserModel existingUser = new UserModel();
        existingUser.setRole("USER");
        existingUser.setEmail(newUser.getEmail());
        
        when(repo.findByEmail(newUser.getEmail())).thenReturn(existingUser);
        
        doNothing().when(repo).updateUser(anyString(), anyString(), anyString());

        ResponseEntity<?> response = userService.updateUser(newUser, validAdminAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repo, times(1)).updateUser(newUser.getEmail(), newUser.getPassword(), newUser.getRole());
    }
    @Test
    void updateUser_Fail_NotFound() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(null);

        assertThrows(NoDataFoundException.class, () -> {
            userService.updateUser(newUser, validAdminAuth);
        });
    }

    @Test
    void deleteUser_Success() {
        UserModel user = new UserModel();
        user.setRole("USER");
        when(repo.findById(1)).thenReturn(user);
        doNothing().when(repo).deleteById(1);

        ResponseEntity<?> response = userService.deleteUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repo, times(1)).deleteById(1);
    }

    @Test
    void deleteUser_Fail_Owner() {
        UserModel owner = new UserModel();
        owner.setRole("OWNER");
        when(repo.findById(1)).thenReturn(owner);

        assertThrows(InvalidRequestException.class, () -> {
            userService.deleteUser(1);
        });
        verify(repo, times(0)).deleteById(1);
    }
}