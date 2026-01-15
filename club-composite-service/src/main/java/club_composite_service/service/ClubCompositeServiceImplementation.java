package club_composite_service.service;

import api.composite.club.ClubAggregate;
import api.composite.club.ClubCompositeService;
import api.composite.club.ReviewSummary;
import api.composite.club.ServiceAddresses;
import api.core.club.Club;
import api.core.review.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ClubCompositeServiceImplementation implements ClubCompositeService {

    @Autowired
    private ClubCompositeIntegration integration;

    @Override
    public Mono<ClubAggregate> getClub(int clubId) {
        return Mono.zip(
            integration.getClub(clubId),
            integration.getReviews(clubId).collectList()
        ).map(tuple -> createClubAggregate(tuple.getT1(), tuple.getT2()));
    }

    private ClubAggregate createClubAggregate(Club club, List<Review> reviews) {
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
             reviews.stream()
                .map(r -> new ReviewSummary(r.getId(), r.getUserEmail(), r.getRating(), r.getComment()))
                .collect(Collectors.toList());

        String clubAddress = club.getServiceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress() : "";
        
        ServiceAddresses serviceAddresses = new ServiceAddresses(
            "club-composite",
            clubAddress,
            reviewAddress,
            null,
            null,
            null 
        );

        return new ClubAggregate(
            club.getId(),
            club.getName(),
            club.getLocation(),
            club.getPhoneNumber(),
            reviewSummaries,
            serviceAddresses
        );
    }
}