package club_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import club_service.dto.ClubDto;
import club_service.implementation.ClubServiceImplementation;
import club_service.model.ClubModel;
import club_service.repository.ClubRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository repo;

    @InjectMocks
    private ClubServiceImplementation clubService;

    @Test
    void createClub_Success() {
        ClubDto dto = new ClubDto("New Club", "Location", "123");
        when(repo.existsByName(dto.getName())).thenReturn(Mono.just(false));
        when(repo.save(any(ClubModel.class))).thenReturn(Mono.just(new ClubModel()));

        ResponseEntity<?> response = clubService.createClub(dto).block();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(ClubModel.class));
    }

    @Test
    void createClub_Fail_Duplicate() {
        ClubDto dto = new ClubDto("Existing Club", "Location", "123");
        when(repo.existsByName(dto.getName())).thenReturn(Mono.just(true));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            clubService.createClub(dto).block();
        });
    }

    @Test
    void getClub_Success() {
        ClubModel model = new ClubModel("Test Club", "Loc", "111");
        when(repo.findById(1)).thenReturn(Mono.just(model));

        ResponseEntity<ClubDto> response = clubService.getClub(1).block();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Club", response.getBody().getName());
    }

    @Test
    void getClub_Fail_NotFound() {
        when(repo.findById(99)).thenReturn(Mono.empty());

        assertThrows(NoDataFoundException.class, () -> {
            clubService.getClub(99).block();
        });
    }
}