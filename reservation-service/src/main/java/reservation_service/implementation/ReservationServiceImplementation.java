package reservation_service.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import reservation_service.dto.ReservationDto;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;
import reservation_service.service.ReservationService;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import util.exceptions.NoDataFoundException;
import util.exceptions.InvalidRequestException;

@RestController
public class ReservationServiceImplementation implements ReservationService {

    @Autowired
    private ReservationRepository repo;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public ResponseEntity<?> createReservation(ReservationDto dto) {
        try {
            restTemplate.getForEntity("http://user-service/user/email/" + dto.getUserEmail(), Object.class);
        } catch (HttpClientErrorException e) {
        	throw new InvalidRequestException("User email not found: " + dto.getUserEmail());
        }

        try {
            restTemplate.getForEntity("http://club-service/club/" + dto.getClubId(), Object.class);
        } catch (HttpClientErrorException e) {
        	throw new InvalidRequestException("Club ID not found: " + dto.getClubId());
        }

        ReservationModel model = new ReservationModel(
            dto.getUserEmail(), 
            dto.getClubId(), 
            dto.getCourtNumber(), 
            dto.getReservationTime()
        );
        ReservationModel saved = repo.save(model);
        dto.setId(saved.getId());
        

        NotificationRequest notification = new NotificationRequest(
            dto.getUserEmail(),
            "Reservation Confirmed",
            "Your reservation for court " + dto.getCourtNumber() + " is confirmed."
        );
        

        rabbitTemplate.convertAndSend("notification-queue", notification);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    public ResponseEntity<?> getReservation(int id) {
        Optional<ReservationModel> model = repo.findById(id);
        if (model.isPresent()) {
            return ResponseEntity.ok(convertToDto(model.get()));
        }
        throw new NoDataFoundException("Reservation not found");
    }

    @Override
    public List<ReservationDto> getReservationsByUser(String email) {
        List<ReservationModel> models = repo.findByUserEmail(email);
        List<ReservationDto> dtos = new ArrayList<>();
        for (ReservationModel m : models) {
            dtos.add(convertToDto(m));
        }
        return dtos;
    }

    @Override
    public ResponseEntity<?> deleteReservation(int id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.ok("Reservation deleted");
        }
        throw new NoDataFoundException("Reservation not found");
    }

    private ReservationDto convertToDto(ReservationModel m) {
        return new ReservationDto(m.getId(), m.getUserEmail(), m.getClubId(), m.getCourtNumber(), m.getReservationTime());
    }
    
    public static class NotificationRequest {
        public String recipient;
        public String subject;
        public String message;
        
        public NotificationRequest(String recipient, String subject, String message) {
            this.recipient = recipient;
            this.subject = subject;
            this.message = message;
        }
        public String getRecipient() { return recipient; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
    }
}
