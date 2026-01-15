package api.core.review;

import java.time.LocalDate;

public class Review {
    private int id;
    private int clubId;
    private String userEmail;
    private int rating;
    private String comment;
    private LocalDate reviewDate;
    private String serviceAddress;

    public Review() {}

    public Review(int id, int clubId, String userEmail, int rating, String comment, LocalDate reviewDate, String serviceAddress) {
        this.id = id;
        this.clubId = clubId;
        this.userEmail = userEmail;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.serviceAddress = serviceAddress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public String getServiceAddress() { return serviceAddress; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
}