package reservation_service.dto;

import java.time.LocalDateTime;

public class ReservationDto {
    private int id;
    private String userEmail;
    private int clubId;
    private int courtNumber;
    private LocalDateTime reservationTime;

    public ReservationDto() {}

    public ReservationDto(int id, String userEmail, int clubId, int courtNumber, LocalDateTime reservationTime) {
        this.id = id;
        this.userEmail = userEmail;
        this.clubId = clubId;
        this.courtNumber = courtNumber;
        this.reservationTime = reservationTime;
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
}