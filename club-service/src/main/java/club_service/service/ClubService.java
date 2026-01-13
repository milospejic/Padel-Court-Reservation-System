package club_service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import club_service.dto.ClubDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClubService {

    @GetMapping("/club")
    Flux<ClubDto> getClubs();

    @GetMapping("/club/{id}")
    Mono<ResponseEntity<ClubDto>> getClub(@PathVariable int id);

    @PostMapping("/club")
    Mono<ResponseEntity<?>> createClub(@RequestBody ClubDto dto);

    @DeleteMapping("/club/{id}")
    Mono<ResponseEntity<?>> deleteClub(@PathVariable int id);
}