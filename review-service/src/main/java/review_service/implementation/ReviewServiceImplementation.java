package review_service.implementation;

import api.core.review.Review;
import api.core.review.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import util.exceptions.InvalidRequestException;

import java.time.LocalDate;

@RestController
public class ReviewServiceImplementation implements ReviewService {

    @Autowired
    private ReviewRepository repo;

    @Override
    public Flux<Review> getReviews(int clubId) {
        return repo.findByClubId(clubId)
                .map(this::entityToApi);
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.getClubId() <= 0) {
            return Mono.error(new InvalidRequestException("Invalid clubId: " + body.getClubId()));
        }
        if (body.getRating() < 1 || body.getRating() > 5) {
            return Mono.error(new InvalidRequestException("Rating must be between 1 and 5"));
        }
        return repo.save(apiToEntity(body))
                .map(this::entityToApi);
    }

    @Override
    public Mono<Void> deleteReview(int id) {
        return repo.deleteById(id);
    }

    private Review entityToApi(ReviewModel entity) {
        return new Review(
            entity.getId(),
            entity.getClubId(),
            entity.getUserEmail(),
            entity.getRating(),
            entity.getComment(),
            entity.getReviewDate(),
            null 
        );
    }

    private ReviewModel apiToEntity(Review api) {
        return new ReviewModel(
            api.getUserEmail(),
            api.getClubId(),
            api.getRating(),
            api.getComment(),
            (api.getReviewDate() != null) ? api.getReviewDate() : LocalDate.now()
        );
    }
}