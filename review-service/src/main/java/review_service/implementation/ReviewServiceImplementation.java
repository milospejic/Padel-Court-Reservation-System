package review_service.implementation;

import api.core.review.Review;
import api.core.review.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import review_service.mapper.ReviewMapper;
import review_service.repository.ReviewRepository;
import util.exceptions.InvalidRequestException;

@RestController
public class ReviewServiceImplementation implements ReviewService {

    private final ReviewRepository repo;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImplementation(ReviewRepository repo, ReviewMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Flux<Review> getReviews(int clubId) {
        return repo.findByClubId(clubId)
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.getClubId() <= 0) {
            return Mono.error(new InvalidRequestException("Invalid clubId: " + body.getClubId()));
        }
        if (body.getRating() < 1 || body.getRating() > 5) {
            return Mono.error(new InvalidRequestException("Rating must be between 1 and 5"));
        }
        return repo.save(mapper.apiToEntity(body))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteReview(int id) {
        return repo.deleteById(id);
    }
}