package api.composite.club;

public class ReviewSummary {
    private final int id;
    private final String userEmail;
    private final int rating;
    private final String comment;

    public ReviewSummary(int id, String userEmail, int rating, String comment) {
        this.id = id;
        this.userEmail = userEmail;
        this.rating = rating;
        this.comment = comment;
    }

    public int getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}