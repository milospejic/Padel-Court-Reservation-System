package club_service.implementation;

import club_service.dto.ClubDto;
import club_service.model.ClubModel;
import club_service.repository.ClubRepository;
import club_service.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@RestController
public class ClubServiceImplementation implements ClubService {

    @Autowired
    private ClubRepository repo;

    @Override
    public Flux<ClubDto> getClubs() {
        return repo.findAll()
                .map(model -> new ClubDto(model.getName(), model.getLocation(), model.getPhoneNumber()));
    }

    @Override
    public Mono<ResponseEntity<ClubDto>> getClub(int id) {
        return repo.findById(id)
                .map(model -> new ClubDto(model.getName(), model.getLocation(), model.getPhoneNumber()))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new NoDataFoundException("Club not found")));
    }

    @Override
    public Mono<ResponseEntity<?>> createClub(ClubDto dto) {
        return repo.existsByName(dto.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new EntityAlreadyExistsException("Club already exists"));
                    }
                    ClubModel model = new ClubModel(dto.getName(), dto.getLocation(), dto.getPhoneNumber());
                    return repo.save(model);
                })
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body("Club created successfully"));
    }

    @Override
    public Mono<ResponseEntity<?>> deleteClub(int id) {
        return repo.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return repo.deleteById(id)
                                .then(Mono.just(ResponseEntity.ok("Club deleted successfully")));
                    }
                    return Mono.error(new NoDataFoundException("Club not found"));
                });
    }
}