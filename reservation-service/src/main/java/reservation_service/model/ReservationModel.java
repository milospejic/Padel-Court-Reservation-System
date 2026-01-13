package reservation_service.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("reservation_model")
public class ReservationModel {

    @Id
    private Integer id;

    @Column("user_email")
    private String userEmail;

    @Column("club_id")
    private Integer clubId;
    
    @Column("court_number")
    private Integer courtNumber;
    
    @Column("reservation_time")
    private LocalDateTime reservationTime;

    public ReservationModel() {}

    public ReservationModel(String userEmail, Integer clubId, Integer courtNumber, LocalDateTime reservationTime) {
        this.userEmail = userEmail;
        this.clubId = clubId;
        this.courtNumber = courtNumber;
        this.reservationTime = reservationTime;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public Integer getCourtNumber() { return courtNumber; }
    public void setCourtNumber(Integer courtNumber) { this.courtNumber = courtNumber; }

    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }
}