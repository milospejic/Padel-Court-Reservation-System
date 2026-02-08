package user_service.mapper;

import api.core.user.User;
import user_service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.http.ServiceUtil;

@Component
public class UserMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public UserMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public User entityToApi(UserModel entity) {
        return new User(
            entity.getId(),
            entity.getEmail(),
            null,
            entity.getRole(),
            serviceUtil.getServiceAddress()
        );
    }

    public UserModel apiToEntity(User api) {
        return new UserModel(
            api.getEmail(),
            api.getPassword(),
            api.getRole()
        );
    }
}