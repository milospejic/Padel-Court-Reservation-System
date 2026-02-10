package api.core.reservation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationService {
    Mono<Reservation> createReservation(@RequestBody Reservation body);
    Flux<Reservation> getReservations(@RequestParam(value = "email", required = false) String email);
    Mono<Void> deleteReservation(@PathVariable int id);
}