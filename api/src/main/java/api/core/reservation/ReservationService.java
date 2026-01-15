package api.core.reservation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationService {
    @PostMapping(value = "/reservation", consumes = "application/json", produces = "application/json")
    Mono<Reservation> createReservation(@RequestBody Reservation body);

    @GetMapping(value = "/reservation", produces = "application/json")
    Flux<Reservation> getReservations(@RequestParam(value = "email", required = false) String email);
    
    @DeleteMapping(value = "/reservation/{id}")
    Mono<Void> deleteReservation(@PathVariable int id);
}