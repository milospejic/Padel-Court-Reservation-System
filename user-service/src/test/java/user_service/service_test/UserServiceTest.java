package user_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString; // Import this
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import reactor.core.publisher.Mono;

import api.core.user.User;
import user_service.implementation.UserServiceImplementation;
import user_service.mapper.UserMapper;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;
import util.exceptions.EntityAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceRepository repo;
    
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplementation userService;

    private User newUser;

    @BeforeEach
    void setUp() {
        newUser = new User(0, "new@uns.ac.rs", "password", "USER", null);
    }

    @Test
    void createUser_Success() {
        when(repo.findByEmail(anyString())).thenReturn(Mono.empty());
        
        String fakeHash = "$2a$12$fakeHashStringForTestingPurposesOnly";
        when(passwordEncoder.encode(anyString())).thenReturn(fakeHash);
        
        UserModel savedModel = new UserModel(newUser.getEmail(), fakeHash, newUser.getRole());
        savedModel.setId(1);
        when(userMapper.apiToEntity(any(User.class))).thenReturn(savedModel);

        when(repo.save(any(UserModel.class))).thenReturn(Mono.just(savedModel));

        when(userMapper.entityToApi(any(UserModel.class))).thenReturn(newUser);

        User response = userService.createUser(newUser).block();

        assertNotNull(response);
        verify(userMapper).apiToEntity(any(User.class));
        verify(userMapper).entityToApi(any(UserModel.class));
    }

    @Test
    void createUser_Fail_AlreadyExists() {
        when(repo.findByEmail(newUser.getEmail())).thenReturn(Mono.just(new UserModel()));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.createUser(newUser).block();
        });

        verify(repo, times(0)).save(any(UserModel.class));
    }

    @Test
    void getUser_Success() {
        UserModel user = new UserModel("test@email.com", "hashed_pass", "USER");
        user.setId(1);
        
        User expectedApiUser = new User(1, "test@email.com", null, "USER", "mock-address");

        when(repo.findById(1)).thenReturn(Mono.just(user));
        
        when(userMapper.entityToApi(any(UserModel.class))).thenReturn(expectedApiUser);

        User response = userService.getUser(1).block();

        assertNotNull(response);
        assertEquals("test@email.com", response.getEmail());
        assertEquals(1, response.getId());
        verify(userMapper, times(1)).entityToApi(any(UserModel.class));
    }
}