package com.residuosolido.app;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;

import java.util.List;

/**
 * Fixtures compartidas para tests que necesitan un User "listo para operar"
 * (ciudadano o organización), en vez de repetir new User() + setters sueltos.
 * Mismo criterio que citizen()/org() en scratch/App.java — ver docs/MEJORAS.md.
 */
public final class TestFixtures {

    private TestFixtures() {}

    public static User citizen(String id, String phone) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.USER);
        u.setActive(true);
        u.setPhone(phone);
        return u;
    }

    public static User organization(String id, City city, MaterialCategory... materials) {
        User o = new User();
        o.setId(id);
        o.setRole(Role.ORGANIZATION);
        o.setCity(city);
        o.setActive(true);
        o.setPhone("+59899123456");
        o.setProfileCompleted(true);
        o.setAcceptedMaterials(materials.length > 0 ? List.of(materials) : List.of());
        return o;
    }
}
