package club_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import api.core.club.Club; 
import club_service.implementation.ClubServiceImplementation;
import club_service.model.ClubModel;
import club_service.repository.ClubRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock private ClubRepository repo;

    @InjectMocks
    private ClubServiceImplementation clubService;

    @Test
    void createClub_Success() {
        Club apiClub = new Club(0, "New Club", "Location", "123", null);
        
        when(repo.existsByName(apiClub.getName())).thenReturn(Mono.just(false));
        ClubModel savedEntity = new ClubModel("New Club", "Location", "123");
        savedEntity.setId(1);
        
        when(repo.save(any(ClubModel.class))).thenReturn(Mono.just(savedEntity));

        Club response = clubService.createClub(apiClub).block();

        assertNotNull(response);
        assertEquals("New Club", response.getName());
        verify(repo, times(1)).save(any(ClubModel.class));
    }

    @Test
    void createClub_Fail_Duplicate() {
        Club apiClub = new Club(0, "Existing Club", "Location", "123", null);
        when(repo.existsByName(apiClub.getName())).thenReturn(Mono.just(true));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            clubService.createClub(apiClub).block();
        });
    }

    @Test
    void getClub_Success() {
        ClubModel model = new ClubModel("Test Club", "Loc", "111");
        model.setId(1);
        when(repo.findById(1)).thenReturn(Mono.just(model));

        Club response = clubService.getClub(1).block();

        assertNotNull(response);
        assertEquals("Test Club", response.getName());
    }

    @Test
    void getClub_Fail_NotFound() {
        when(repo.findById(99)).thenReturn(Mono.empty());

        assertThrows(NoDataFoundException.class, () -> {
            clubService.getClub(99).block();
        });
    }
}