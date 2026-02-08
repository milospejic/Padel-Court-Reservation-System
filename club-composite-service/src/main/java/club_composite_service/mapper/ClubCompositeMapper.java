package club_composite_service.mapper;

import api.composite.club.ClubAggregate;
import api.composite.club.ReviewSummary;
import api.composite.club.ServiceAddresses;
import api.core.club.Club;
import api.core.review.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.http.ServiceUtil;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClubCompositeMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public ClubCompositeMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public ClubAggregate createClubAggregate(Club club, List<Review> reviews) {
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
             reviews.stream()
                .map(r -> new ReviewSummary(r.getId(), r.getUserEmail(), r.getRating(), r.getComment()))
                .collect(Collectors.toList());

        String clubAddress = club.getServiceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress() : "";
        
        ServiceAddresses serviceAddresses = new ServiceAddresses(
            serviceUtil.getServiceAddress(),
            clubAddress,
            reviewAddress
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