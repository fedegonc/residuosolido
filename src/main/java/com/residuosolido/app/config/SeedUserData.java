package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public class SeedUserData {

    public record SeedUsers(User u1, User u2, User u3, User u4,
                             User o1, User o2, User o3, User o4, User o5, User o6) {}

    public static SeedUsers seedAll(UserRepository repo, PasswordEncoder encoder) {
        User u1 = SeedDataFactory.createUser(repo, encoder, "juan", "juan@mail.com", "Juan Pérez", Role.USER, "+598 99 123 456", City.RIVERA);
        User u2 = SeedDataFactory.createUser(repo, encoder, "maria", "maria@mail.com", "María García", Role.USER, "+55 55 654 321", City.LIVRAMENTO);
        User u3 = SeedDataFactory.createUser(repo, encoder, "pedro", "pedro@mail.com", "Pedro Martínez", Role.USER, "+598 99 222 333", City.RIVERA);
        User u4 = SeedDataFactory.createUser(repo, encoder, "lucia", "lucia@mail.com", "Lucía Fernández", Role.USER, "+55 55 111 222", City.LIVRAMENTO);
        User o1 = SeedDataFactory.createOrg(repo, encoder, "coopverde", "coopverde@mail.com", "Cooperativa Verde", "+598 99 111 222", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.VIDRIO));
        User o2 = SeedDataFactory.createOrg(repo, encoder, "reciclarivera", "reciclarivera@mail.com", "ReciclaRivera", "+598 99 333 444", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO));
        User o3 = SeedDataFactory.createOrg(repo, encoder, "ecofrontera", "ecofrontera@mail.com", "EcoFrontera", "+598 99 555 666", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO, MaterialCategory.METAL));
        User o4 = SeedDataFactory.createOrg(repo, encoder, "reciclart", "reciclart@mail.com", "ReciclaRT", "+55 55 333 444", City.LIVRAMENTO,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.METAL));
        User o5 = SeedDataFactory.createOrg(repo, encoder, "coopesperanca", "coopesperanca@mail.com", "Cooperativa Esperança", "+55 55 777 888", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO));
        User o6 = SeedDataFactory.createOrg(repo, encoder, "verdefronteira", "verdefronteira@mail.com", "Verde Fronteira", "+55 55 999 000", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO, MaterialCategory.METAL, MaterialCategory.PLASTICO));
        return new SeedUsers(u1, u2, u3, u4, o1, o2, o3, o4, o5, o6);
    }
}
