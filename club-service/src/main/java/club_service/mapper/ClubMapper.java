package club_service.mapper;

import api.core.club.Club;
import club_service.model.ClubModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.http.ServiceUtil;

@Component
public class ClubMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public ClubMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public Club entityToApi(ClubModel entity) {
        return new Club(
            entity.getId(),
            entity.getName(),
            entity.getLocation(),
            entity.getPhoneNumber(),
            serviceUtil.getServiceAddress() 
        );
    }

    public ClubModel apiToEntity(Club api) {
        return new ClubModel(
            api.getName(),
            api.getLocation(),
            api.getPhoneNumber()
        );
    }
}