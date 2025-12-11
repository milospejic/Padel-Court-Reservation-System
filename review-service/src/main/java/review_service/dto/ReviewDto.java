package review_service.dto;

import java.time.LocalDate;

public class ReviewDto {
    private int id;
    private String userEmail;
    private int clubId;
    private int rating;
    private String comment;
    private LocalDate reviewDate;

    public ReviewDto() {}

    public ReviewDto(int id, String userEmail, int clubId, int rating, String comment, LocalDate reviewDate) {
        this.id = id;
        this.userEmail = userEmail;
        this.clubId = clubId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}