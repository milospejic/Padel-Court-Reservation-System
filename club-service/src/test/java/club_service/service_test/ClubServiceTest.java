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
import club_service.mapper.ClubMapper;
import club_service.model.ClubModel;
import club_service.repository.ClubRepository;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class ClubServiceTest {
    @Mock private ClubRepository repo;
    @Mock private ClubMapper clubMapper; // Add this

    @InjectMocks
    private ClubServiceImplementation clubService;

    @Test
    void createClub_Success() {
        Club apiClub = new Club(0, "New Club", "Location", "123", null);
        ClubModel entity = new ClubModel("New Club", "Location", "123");
        
        when(repo.existsByName(anyString())).thenReturn(Mono.just(false));
        when(clubMapper.apiToEntity(any(Club.class))).thenReturn(entity); // Stub Mapper
        when(repo.save(any(ClubModel.class))).thenReturn(Mono.just(entity));
        when(clubMapper.entityToApi(any(ClubModel.class))).thenReturn(apiClub); // Stub Mapper

        Club response = clubService.createClub(apiClub).block();

        assertNotNull(response);
        verify(clubMapper).apiToEntity(any());
    }


    @Test
    void createClub_Fail_Duplicate() {
        Club apiClub = new Club(0, "Existing Club", "Location", "123", null);
        when(repo.existsByName(apiClub.getName())).thenReturn(Mono.just(true));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            clubService.createClub(apiClub).block();
        });


        verify(repo, never()).save(any());
        verify(clubMapper, never()).apiToEntity(any());
    }

    @Test
    void getClub_Success() {
        ClubModel model = new ClubModel("Test Club", "Loc", "111");
        model.setId(1);
        Club expectedApiClub = new Club(1, "Test Club", "Loc", "111", "mock-addr");

        when(repo.findById(1)).thenReturn(Mono.just(model));
        
        when(clubMapper.entityToApi(any(ClubModel.class))).thenReturn(expectedApiClub);

        Club response = clubService.getClub(1).block();

        assertNotNull(response);
        assertEquals("Test Club", response.getName());
        verify(clubMapper).entityToApi(model);
    }

    @Test
    void getClub_Fail_NotFound() {
        when(repo.findById(99)).thenReturn(Mono.empty());

        assertThrows(NoDataFoundException.class, () -> {
            clubService.getClub(99).block();
        });

        verify(clubMapper, never()).entityToApi(any());
    }
}