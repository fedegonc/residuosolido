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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               RequestRepository requestRepository,
                               InformalCollectorRepository informalCollectorRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.seed:false}") boolean shouldSeed) {
        return args -> {
            if (!shouldSeed) {
                return;
            }

            logger.info("=== Iniciando carga de datos de prueba ===");

            if (userRepository.findByUsername("admin").isPresent()) {
                logger.info("Datos ya cargados. Usa --seed solo en BD limpia.");
                return;
            }

            // --- Usuarios ---
            User admin = createUser(userRepository, passwordEncoder, "admin", "admin@residuo.com", "Admin", Role.USER, null, null);
            User user1 = createUser(userRepository, passwordEncoder, "juan", "juan@mail.com", "Juan Pérez", Role.USER, "099123456", City.RIVERA);
            User user2 = createUser(userRepository, passwordEncoder, "maria", "maria@mail.com", "María García", Role.USER, "099654321", City.LIVRAMENTO);

            // --- Organizaciones ---
            User org1 = createOrg(userRepository, passwordEncoder, "coopverde", "coopverde@mail.com", "Cooperativa Verde", "099111222", City.RIVERA);
            User org2 = createOrg(userRepository, passwordEncoder, "reciclart", "reciclart@mail.com", "ReciclaRT", "099333444", City.LIVRAMENTO);

            logger.info("Usuarios creados: admin, juan, maria, coopverde, reciclart (password: 12345678)");

            // --- Solicitudes ---
            createRequest(requestRepository, user1, org1, "Calle 18 de Julio 123", "Frente al supermercado", City.RIVERA,
                    List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL), RequestStatus.PENDING, null);

            createRequest(requestRepository, user2, org2, "Rua dos Andradas 456", "Esquina con farmacia", City.LIVRAMENTO,
                    List.of(MaterialCategory.VIDRIO), RequestStatus.IN_PROGRESS, TimeSlot.MANANA);

            createRequest(requestRepository, user1, org1, "Av. Sarandí 789", "Portón verde", City.RIVERA,
                    List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.TARDE);

            createRequest(requestRepository, user2, null, "Rua Flores da Cunha 32", null, City.LIVRAMENTO,
                    List.of(MaterialCategory.PAPEL), RequestStatus.PENDING, null);

            createRequest(requestRepository, null, null, "Calle Misiones 55", "Casa con rejas", City.RIVERA,
                    List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO, MaterialCategory.METAL), RequestStatus.REJECTED, null,
                    "Carlos Guest", "099888777");

            logger.info("Solicitudes creadas: 5 (2 pendientes, 1 en proceso, 1 completada, 1 rechazada)");

            // --- Catadores informales ---
            createCollector(informalCollectorRepository, org1.getId(), "Pedro Silva", "099555666", City.RIVERA,
                    List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL), "Catador de la zona norte");
            createCollector(informalCollectorRepository, org1.getId(), "Ana Souza", "099777888", City.RIVERA,
                    List.of(MaterialCategory.VIDRIO, MaterialCategory.METAL), "Recoge vidrio los viernes");

            logger.info("Catadores informales creados: 2");
            logger.info("=== Carga de datos completada ===");
            logger.info("Credenciales: admin/juan/maria (USER), coopverde/reciclart (ORG) — password: 12345678");
        };
    }

    private User createUser(UserRepository repo, PasswordEncoder encoder, String username, String email, String firstName, Role role, String phone, City city) {
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

    private User createOrg(UserRepository repo, PasswordEncoder encoder, String username, String email, String firstName, String phone, City city) {
        return createUser(repo, encoder, username, email, firstName, Role.ORGANIZATION, phone, city);
    }

    private void createRequest(RequestRepository repo, User user, User org, String address, String ref, City city,
                               List<MaterialCategory> materials, RequestStatus status, TimeSlot slot) {
        createRequest(repo, user, org, address, ref, city, materials, status, slot, null, null);
    }

    private void createRequest(RequestRepository repo, User user, User org, String address, String ref, City city,
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

    private void createCollector(InformalCollectorRepository repo, String orgId, String name, String phone, City city,
                                 List<MaterialCategory> materials, String notes) {
        InformalCollector c = InformalCollector.create(orgId, name, phone, city, materials, notes);
        repo.save(c);
    }
}
