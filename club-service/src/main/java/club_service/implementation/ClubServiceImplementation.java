package club_service.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import club_service.dto.ClubDto;
import club_service.model.ClubModel;
import club_service.repository.ClubRepository;
import club_service.service.ClubService;
import util.exceptions.EntityAlreadyExistsException;
import util.exceptions.NoDataFoundException;

@RestController
public class ClubServiceImplementation implements ClubService {

    @Autowired
    private ClubRepository repo;

    @Override
    public List<ClubDto> getClubs() {
        List<ClubModel> models = repo.findAll();
        List<ClubDto> dtos = new ArrayList<>();
        for (ClubModel model : models) {
            dtos.add(new ClubDto(model.getName(), model.getLocation(), model.getPhoneNumber()));
        }
        return dtos;
    }

    @Override
    public ResponseEntity<ClubDto> getClub(int id) {
        Optional<ClubModel> model = repo.findById(id);
        if (model.isPresent()) {
            ClubModel m = model.get();
            return ResponseEntity.ok(new ClubDto(m.getName(), m.getLocation(), m.getPhoneNumber()));
        }
        throw new NoDataFoundException("Club not found");
    }

    @Override
    public ResponseEntity<?> createClub(ClubDto dto) {
        if (repo.existsByName(dto.getName())) {
             throw new EntityAlreadyExistsException("Club already exists");
        }
        ClubModel model = new ClubModel(dto.getName(), dto.getLocation(), dto.getPhoneNumber());
        repo.save(model);
        return ResponseEntity.status(HttpStatus.CREATED).body("Club created successfully");
    }

    @Override
    public ResponseEntity<?> deleteClub(int id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.ok("Club deleted successfully");
        }
        throw new NoDataFoundException("Club not found");
    }
}