package review_service.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("review_model")
public class ReviewModel {

    @Id
    private Integer id;

    @Column("user_email")
    private String userEmail;

    @Column("club_id")
    private Integer clubId;

    @Column("rating")
    private Integer rating; 
    
    @Column("comment")
    private String comment;
    
    @Column("review_date")
    private LocalDate reviewDate;

    public ReviewModel() {}

    public ReviewModel(String userEmail, Integer clubId, Integer rating, String comment, LocalDate reviewDate) {
        this.userEmail = userEmail;
        this.clubId = clubId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}