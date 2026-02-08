package club_composite_service.service;

import api.composite.club.ClubAggregate;
import api.composite.club.ClubCompositeService;
import club_composite_service.mapper.ClubCompositeMapper; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ClubCompositeServiceImplementation implements ClubCompositeService {

    private final ClubCompositeIntegration integration;
    private final ClubCompositeMapper mapper; 

    @Autowired
    public ClubCompositeServiceImplementation(ClubCompositeIntegration integration, ClubCompositeMapper mapper) {
        this.integration = integration;
        this.mapper = mapper;
    }

    @Override
    public Mono<ClubAggregate> getClub(int clubId) {
        return Mono.zip(
            integration.getClub(clubId),
            integration.getReviews(clubId).collectList()
        ).map(tuple -> mapper.createClubAggregate(tuple.getT1(), tuple.getT2()));
    }
}