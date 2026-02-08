package review_service.mapper;

import api.core.review.Review;
import review_service.model.ReviewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.http.ServiceUtil;
import java.time.LocalDate;

@Component
public class ReviewMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public Review entityToApi(ReviewModel entity) {
        return new Review(
            entity.getId(),
            entity.getClubId(),
            entity.getUserEmail(),
            entity.getRating(),
            entity.getComment(),
            entity.getReviewDate(),
            serviceUtil.getServiceAddress()
        );
    }

    public ReviewModel apiToEntity(Review api) {
        return new ReviewModel(
            api.getUserEmail(),
            api.getClubId(),
            api.getRating(),
            api.getComment(),
            (api.getReviewDate() != null) ? api.getReviewDate() : LocalDate.now()
        );
    }
}