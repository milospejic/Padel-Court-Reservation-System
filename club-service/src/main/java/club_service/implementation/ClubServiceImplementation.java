package club_service.implementation;

import api.core.club.Club;
import api.core.club.ClubService;
import club_service.model.ClubModel;
import club_service.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Flux<Club> getClubs() {
        return repo.findAll()
                .map(this::entityToApi);
    }

    @Override
    public Mono<Club> getClub(int id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("Club not found: " + id)))
                .map(this::entityToApi);
    }

    @Override
    public Mono<Club> createClub(Club body) {
        return repo.existsByName(body.getName())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new EntityAlreadyExistsException("Club already exists: " + body.getName()));
                    }
                    return repo.save(apiToEntity(body));
                })
                .map(this::entityToApi);
    }

    @Override
    public Mono<Void> deleteClub(int id) {
        return repo.existsById(id)
                .flatMap(exists -> {
                    if (!Boolean.TRUE.equals(exists)) {
                        return Mono.error(new NoDataFoundException("Club not found: " + id));
                    }
                    return repo.deleteById(id);
                });
    }

    private Club entityToApi(ClubModel entity) {
        return new Club(
            entity.getId(), 
            entity.getName(), 
            entity.getLocation(), 
            entity.getPhoneNumber(), 
            null
        );
    }

    private ClubModel apiToEntity(Club api) {
        return new ClubModel(api.getName(), api.getLocation(), api.getPhoneNumber());
    }
}