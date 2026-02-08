package api.composite.club;

public class ServiceAddresses {
    private final String compositeAddress;
    private final String clubAddress;
    private final String reviewAddress;


    public ServiceAddresses() {
        this.compositeAddress = null;
        this.clubAddress = null;
        this.reviewAddress = null;
    }

    public ServiceAddresses(String compositeAddress, String clubAddress, String reviewAddress) {
        this.compositeAddress = compositeAddress;
        this.clubAddress = clubAddress;
        this.reviewAddress = reviewAddress;
    }

    public String getCompositeAddress() { return compositeAddress; }
    public String getClubAddress() { return clubAddress; }
    public String getReviewAddress() { return reviewAddress; }

}