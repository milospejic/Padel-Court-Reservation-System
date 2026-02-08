package club_service.implementation;

import api.core.club.Club;
import api.core.club.ClubService;
import club_service.mapper.ClubMapper;
import club_service.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@RestController
public class ClubServiceImplementation implements ClubService {

    private final ClubRepository repo;
    private final ClubMapper mapper;

    @Autowired
    public ClubServiceImplementation(ClubRepository repo, ClubMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Flux<Club> getClubs() {
        return repo.findAll()
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Club> getClub(int id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("Club not found: " + id)))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Club> createClub(Club body) {
        return repo.existsByName(body.getName())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new EntityAlreadyExistsException("Club already exists: " + body.getName()));
                    }
                    return repo.save(mapper.apiToEntity(body));
                })
                .map(mapper::entityToApi);
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
}