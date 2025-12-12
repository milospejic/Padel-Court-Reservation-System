package reservation_service.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reservation_service.dto.ReservationDto;

public interface ReservationService {
    
    @PostMapping("/reservation")
    ResponseEntity<?> createReservation(@RequestBody ReservationDto dto);

    @GetMapping("/reservation/{id}")
    ResponseEntity<?> getReservation(@PathVariable int id);

    @GetMapping("/reservation/user/{email}")
    ResponseEntity<List<ReservationDto>> getReservationsByUser(@PathVariable String email);
    
    @DeleteMapping("/reservation/{id}")
    ResponseEntity<?> deleteReservation(@PathVariable int id);
}