package club_service.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import club_service.dto.ClubDto;

public interface ClubService {

    @GetMapping("/club")
    List<ClubDto> getClubs();

    @GetMapping("/club/{id}")
    ResponseEntity<ClubDto> getClub(@PathVariable int id);

    @PostMapping("/club")
    ResponseEntity<?> createClub(@RequestBody ClubDto dto);

    @DeleteMapping("/club/{id}")
    ResponseEntity<?> deleteClub(@PathVariable int id);
}