package reservation_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ReservationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private int clubId;

    private int courtNumber;
    
    @Column(nullable = false)
    private LocalDateTime reservationTime;

    public ReservationModel() {}

    public ReservationModel(String userEmail, int clubId, int courtNumber, LocalDateTime reservationTime) {
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