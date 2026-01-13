package reservation_service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reservation_service.dto.ReservationDto;

public interface ReservationService {
    
    @PostMapping("/reservation")
    Mono<ResponseEntity<?>> createReservation(@RequestBody ReservationDto dto);

    @GetMapping("/reservation/{id}")
    Mono<ResponseEntity<?>> getReservation(@PathVariable int id);

    @GetMapping("/reservation/user/{email}")
    Mono<ResponseEntity<Flux<ReservationDto>>> getReservationsByUser(@PathVariable String email);
    
    @DeleteMapping("/reservation/{id}")
    Mono<ResponseEntity<?>> deleteReservation(@PathVariable int id);
}