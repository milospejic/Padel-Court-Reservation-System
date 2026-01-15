package api.composite.club;

public class ServiceAddresses {
    private final String compositeAddress;
    private final String clubAddress;
    private final String reviewAddress;
    private final String userAddress;
    private final String reservationAddress;
    private final String notificationAddress;

    public ServiceAddresses() {
        this.compositeAddress = null;
        this.clubAddress = null;
        this.reviewAddress = null;
        this.userAddress = null;
        this.reservationAddress = null;
        this.notificationAddress = null;
    }

    public ServiceAddresses(String compositeAddress, String clubAddress, String reviewAddress, 
                            String userAddress, String reservationAddress, String notificationAddress) {
        this.compositeAddress = compositeAddress;
        this.clubAddress = clubAddress;
        this.reviewAddress = reviewAddress;
        this.userAddress = userAddress;
        this.reservationAddress = reservationAddress;
        this.notificationAddress = notificationAddress; 
    }

    public String getCompositeAddress() { return compositeAddress; }
    public String getClubAddress() { return clubAddress; }
    public String getReviewAddress() { return reviewAddress; }
    public String getUserAddress() { return userAddress; }
    public String getReservationAddress() { return reservationAddress; }
    public String getNotificationAddress() { return notificationAddress; }
}