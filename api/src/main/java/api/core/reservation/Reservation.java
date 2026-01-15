package api.core.reservation;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private String userEmail;
    private int clubId;
    private int courtNumber;
    private LocalDateTime reservationTime;
    private String serviceAddress;

    public Reservation() {}

    public Reservation(int id, String userEmail, int clubId, int courtNumber, LocalDateTime reservationTime, String serviceAddress) {
        this.id = id;
        this.userEmail = userEmail;
        this.clubId = clubId;
        this.courtNumber = courtNumber;
        this.reservationTime = reservationTime;
        this.serviceAddress = serviceAddress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }
    public int getCourtNumber() { return courtNumber; }
    public void setCourtNumber(int courtNumber) { this.courtNumber = courtNumber; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }
    public String getServiceAddress() { return serviceAddress; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
}