package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.InformalCollector;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.InformalCollectorRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

public class SeedDataFactory {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataFactory.class);

    public static User createUser(UserRepository repo, PasswordEncoder encoder,
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

    public static User createOrg(UserRepository repo, PasswordEncoder encoder,
                                  String username, String email, String firstName,
                                  String phone, City city, List<MaterialCategory> acceptedMaterials) {
        User u = createUser(repo, encoder, username, email, firstName, Role.ORGANIZATION, phone, city);
        u.setAcceptedMaterials(acceptedMaterials);
        return repo.save(u);
    }

    public static void createRequest(RequestRepository repo, User user, User org,
                                      String address, String ref, City city,
                                      List<MaterialCategory> materials, RequestStatus status, TimeSlot slot) {
        createRequest(repo, user, org, address, ref, city, materials, status, slot, null, null);
    }

    public static void createRequest(RequestRepository repo, User user, User org,
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

    public static void createCollector(InformalCollectorRepository repo, String orgId,
                                        String name, String phone, City city,
                                        List<MaterialCategory> materials, String notes) {
        InformalCollector c = InformalCollector.create(orgId, name, phone, city, materials, notes);
        repo.save(c);
    }

    public static void seedAll(UserRepository userRepo, RequestRepository requestRepo,
                               InformalCollectorRepository collectorRepo, PasswordEncoder encoder) {
        logger.info("=== Iniciando carga de datos de prueba ===");
        requestRepo.deleteAll();
        userRepo.deleteAll();
        collectorRepo.deleteAll();
        var s = SeedUserData.seedAll(userRepo, encoder);
        SeedRequestData.seedAll(requestRepo, s.u1(), s.u2(), s.u3(), s.u4(), s.o1(), s.o2(), s.o3(), s.o4(), s.o5(), s.o6());
        createCollector(collectorRepo, s.o1().getId(), "Pedro Silva", "099555666", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL), "Catador de la zona norte");
        createCollector(collectorRepo, s.o1().getId(), "Ana Souza", "099777888", City.RIVERA,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.METAL), "Recoge vidrio los viernes");
        logger.info("=== Carga completada — password: 12345678 ===");
    }
}
