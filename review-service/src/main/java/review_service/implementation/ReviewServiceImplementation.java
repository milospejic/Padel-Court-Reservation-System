package review_service.implementation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import review_service.dto.ReviewDto;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import review_service.service.ReviewService;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@RestController
public class ReviewServiceImplementation implements ReviewService {

    @Autowired
    private ReviewRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    @CircuitBreaker(name = "reviewService", fallbackMethod = "addReviewFallback")
    public ResponseEntity<?> addReview(ReviewDto dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new InvalidRequestException("Rating must be between 1 and 5");
        }

        try {
            restTemplate.getForEntity("http://user-service/user/email/" + dto.getUserEmail(), Object.class);
        } catch (HttpClientErrorException e) {
            throw new NoDataFoundException("User email not found: " + dto.getUserEmail());
        }

        try {
            restTemplate.getForEntity("http://club-service/club/" + dto.getClubId(), Object.class);
        } catch (HttpClientErrorException e) {
            throw new NoDataFoundException("Club ID not found: " + dto.getClubId());
        }

        ReviewModel model = new ReviewModel(
            dto.getUserEmail(),
            dto.getClubId(),
            dto.getRating(),
            dto.getComment(),
            LocalDate.now()
        );
        ReviewModel saved = repo.save(model);
        dto.setId(saved.getId());
        dto.setReviewDate(saved.getReviewDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    public ResponseEntity<?> addReviewFallback(ReviewDto dto, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Review Service Unavailable: Could not verify User or Club. " + t.getMessage());
    }

    @Override
    public List<ReviewDto> getReviewsByClub(int clubId) {
        List<ReviewModel> models = repo.findByClubId(clubId);
        return convertToDtoList(models);
    }

    @Override
    public List<ReviewDto> getReviewsByUser(String email) {
        List<ReviewModel> models = repo.findByUserEmail(email);
        return convertToDtoList(models);
    }

    @Override
    public ResponseEntity<?> deleteReview(int id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.ok("Review deleted successfully");
        }
        throw new NoDataFoundException("Review not found with id " + id);
    }

    private List<ReviewDto> convertToDtoList(List<ReviewModel> models) {
        List<ReviewDto> dtos = new ArrayList<>();
        for (ReviewModel m : models) {
            dtos.add(new ReviewDto(m.getId(), m.getUserEmail(), m.getClubId(), m.getRating(), m.getComment(), m.getReviewDate()));
        }
        return dtos;
    }
}