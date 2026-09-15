package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Carga datos de prueba (usuarios + solicitudes) para entorno dev y tests.
 * Una sola clase consolida lo que antes eran SeedDataFactory, SeedUserData y SeedRequestData.
 */
@Configuration
@Profile("dev & !prod & !test")
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               RequestRepository requestRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.seed:false}") boolean shouldSeed) {
        return args -> {
            if (!shouldSeed) return;
            seedAll(userRepository, requestRepository, passwordEncoder);
        };
    }

    /** Punto de entrada para tests y DataLoader. Carga todo si la base está vacía. */
    public static void seedAll(UserRepository userRepo, RequestRepository requestRepo,
                               PasswordEncoder encoder) {
        logger.info("=== Iniciando carga de datos de prueba ===");
        if (userRepo.count() > 0 || requestRepo.count() > 0) {
            logger.info("Seed omitido: la base ya contiene datos");
            return;
        }
        User u1 = createUser(userRepo, encoder, "juan", "juan@mail.com", "Juan Pérez", Role.USER, "+598 99 123 456", City.RIVERA);
        User u2 = createUser(userRepo, encoder, "maria", "maria@mail.com", "María García", Role.USER, "+55 55 654 321", City.LIVRAMENTO);
        User u3 = createUser(userRepo, encoder, "pedro", "pedro@mail.com", "Pedro Martínez", Role.USER, "+598 99 222 333", City.RIVERA);
        User u4 = createUser(userRepo, encoder, "lucia", "lucia@mail.com", "Lucía Fernández", Role.USER, "+55 55 111 222", City.LIVRAMENTO);
        User o1 = createOrg(userRepo, encoder, "coopverde", "coopverde@mail.com", "Cooperativa Verde", "+598 99 111 222", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.VIDRIO));
        User o2 = createOrg(userRepo, encoder, "reciclarivera", "reciclarivera@mail.com", "ReciclaRivera", "+598 99 333 444", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO));
        User o3 = createOrg(userRepo, encoder, "ecofrontera", "ecofrontera@mail.com", "EcoFrontera", "+598 99 555 666", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO, MaterialCategory.METAL));
        User o4 = createOrg(userRepo, encoder, "reciclart", "reciclart@mail.com", "ReciclaRT", "+55 55 333 444", City.LIVRAMENTO,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.METAL));
        User o5 = createOrg(userRepo, encoder, "coopesperanca", "coopesperanca@mail.com", "Cooperativa Esperança", "+55 55 777 888", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO));
        User o6 = createOrg(userRepo, encoder, "verdefronteira", "verdefronteira@mail.com", "Verde Fronteira", "+55 55 999 000", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO, MaterialCategory.METAL, MaterialCategory.PLASTICO));

        createRequest(requestRepo, u1, o1, "Calle 18 de Julio 123", "Frente al supermercado", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL), RequestStatus.PENDING, null);
        createRequest(requestRepo, u2, o4, "Rua dos Andradas 456", "Esquina con farmacia", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO), RequestStatus.IN_PROGRESS, TimeSlot.MANANA);
        createRequest(requestRepo, u1, o1, "Av. Sarandí 789", "Portón verde", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.TARDE);
        createRequest(requestRepo, u2, null, "Rua Flores da Cunha 32", null, City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL), RequestStatus.PENDING, null);
        createRequest(requestRepo, null, null, "Calle Misiones 55", "Casa con rejas", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO, MaterialCategory.METAL), RequestStatus.REJECTED, null,
                "Carlos Guest", "+598 99 888 777");
        createRequest(requestRepo, u3, o2, "Calle Independencia 202", "Al lado de la plaza", City.RIVERA,
                List.of(MaterialCategory.METAL), RequestStatus.PENDING, null);
        createRequest(requestRepo, u4, o5, "Rua Marechal Deodoro 77", "Portão azul", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO), RequestStatus.IN_PROGRESS, TimeSlot.TARDE);
        createRequest(requestRepo, u3, o3, "Calle Rivera 450", "Frente a la escuela", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.COMPLETED, TimeSlot.MANANA);
        createRequest(requestRepo, u4, o6, "Rua Bento Gonçalves 890", "Galpão vermelho", City.LIVRAMENTO,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.METAL), RequestStatus.PENDING, null);
        createRequest(requestRepo, null, o1, "Calle Ituzaingó 30", "Departamento 2B", City.RIVERA,
                List.of(MaterialCategory.PLASTICO), RequestStatus.PENDING, null, "Ana Guest", "+598 99 444 555");
        createRequest(requestRepo, u1, o2, "Calle Amethyst 15", "Casa esquinera", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.NOCHE);
        createRequest(requestRepo, u2, o6, "Rua dos Imigrantes 120", "Frente ao mercado", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.REJECTED, null);

        logger.info("=== Carga de demostración completada ===");
    }

    private static User createUser(UserRepository repo, PasswordEncoder encoder,
                                   String username, String email, String firstName,
                                   Role role, String phone, City city) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode("12345678"));
        u.setRole(role);
        u.setFirstName(firstName);
        u.setPhone(phone);
        u.setCity(city);
        u.setActive(true);
        u.setCreatedAt(LocalDateTime.now());
        u.setProfileCompleted(true);
        return repo.save(u);
    }

    private static User createOrg(UserRepository repo, PasswordEncoder encoder,
                                  String username, String email, String firstName,
                                  String phone, City city, List<MaterialCategory> acceptedMaterials) {
        User u = createUser(repo, encoder, username, email, firstName, Role.ORGANIZATION, phone, city);
        u.setAcceptedMaterials(acceptedMaterials);
        return repo.save(u);
    }

    private static void createRequest(RequestRepository repo, User user, User org,
                                      String address, String ref, City city,
                                      List<MaterialCategory> materials, RequestStatus status, TimeSlot slot) {
        createRequest(repo, user, org, address, ref, city, materials, status, slot, null, null);
    }

    private static void createRequest(RequestRepository repo, User user, User org,
                                      String address, String ref, City city,
                                      List<MaterialCategory> materials, RequestStatus status, TimeSlot slot,
                                      String guestName, String guestPhone) {
        Request r = new Request();
        r.setUser(user);
        r.setOrganization(org);
        r.setGuestName(guestName);
        r.setGuestPhone(guestPhone);
        r.setAddress(address);
        r.setAddressReference(ref);
        r.setCity(city);
        r.setMaterials(materials);
        r.setStatus(status);
        r.setConfirmedSlot(slot);
        r.setCreatedAt(LocalDateTime.now().minusDays((long)(Math.random() * 10)));
        repo.save(r);
    }
}
