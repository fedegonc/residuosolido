package com.residuosolido.app.service;

import com.residuosolido.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    @Autowired
    private UserService userService;

    public User getUserForLocationEdit(Long id) {
        if (id != null) {
            return userService.findById(id).orElse(new User());
        }
        return new User();
    }

    public void saveUserLocation(User userForm) {
        if (userForm.getId() != null) {
            User user = userService.findById(userForm.getId()).orElse(null);
            if (user != null) {
                user.setLatitude(userForm.getLatitude());
                user.setLongitude(userForm.getLongitude());
                userService.save(user);
            }
        }
    }
}
