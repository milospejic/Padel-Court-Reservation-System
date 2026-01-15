package api.core.club;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClubService {
    @GetMapping(value = "/club/{id}", produces = "application/json")
    Mono<Club> getClub(@PathVariable int id);

    @GetMapping(value = "/club", produces = "application/json")
    Flux<Club> getClubs();

    @PostMapping(value = "/club", consumes = "application/json", produces = "application/json")
    Mono<Club> createClub(@RequestBody Club body);

    @DeleteMapping(value = "/club/{id}")
    Mono<Void> deleteClub(@PathVariable int id);
}