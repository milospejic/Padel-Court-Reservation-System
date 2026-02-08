package reservation_service.mapper;

import api.core.reservation.Reservation;
import reservation_service.model.ReservationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.http.ServiceUtil;

@Component
public class ReservationMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReservationMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public Reservation entityToApi(ReservationModel entity) {
        return new Reservation(
            entity.getId(),
            entity.getUserEmail(),
            entity.getClubId(),
            entity.getCourtNumber(),
            entity.getReservationTime(),
            serviceUtil.getServiceAddress()
        );
    }

    public ReservationModel apiToEntity(Reservation api) {
        return new ReservationModel(
            api.getUserEmail(),
            api.getClubId(),
            api.getCourtNumber(),
            api.getReservationTime()
        );
    }
}