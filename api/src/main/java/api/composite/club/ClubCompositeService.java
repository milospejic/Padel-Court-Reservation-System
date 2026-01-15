package api.composite.club;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@Tag(name = "ClubComposite", description = "REST API for composite club information.")
public interface ClubCompositeService {

    @GetMapping(value = "/club-composite/{clubId}", produces = "application/json")
    Mono<ClubAggregate> getClub(@PathVariable int clubId);
}