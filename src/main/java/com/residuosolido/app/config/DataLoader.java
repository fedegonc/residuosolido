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

        seedDefenseDemoData(userRepo, requestRepo, encoder, o4);

        logger.info("=== Carga de demostración completada ===");
    }

    /**
     * Dataset armado por propósito para la defensa de tesis (no por volumen) — ver
     * docs/DEFENSA.md §Dataset de demo. Se agrega DESPUÉS del dataset genérico de arriba,
     * sin tocarlo (4 *BrowserTest loguean con coopverde/juan/etc. de ese dataset).
     *
     * 2 casos del pedido original no tienen representación en el modelo actual — documentado
     * en docs/DEFENSA.md, no resuelto con código nuevo (a propósito):
     *   - "REJECTED con motivo escrito": Request/RequestStatus no tienen campo de motivo.
     *   - "Usuario con locale pt": User no tiene campo locale; el idioma se resuelve por
     *     CityAwareLocaleResolver según la ciudad del usuario logueado, no por preferencia
     *     guardada — un ciudadano con city=LIVRAMENTO ya demuestra el bilingüismo.
     */
    private static void seedDefenseDemoData(UserRepository userRepo, RequestRepository requestRepo, PasswordEncoder encoder, User orgLivramento) {
        User renacer = createOrg(userRepo, encoder, "renacer", "renacer@mail.com", "Cooperativa Renacer",
                "+598 99 100 001", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.CARTON, MaterialCategory.VIDRIO, MaterialCategory.METAL));
        User vidaVerde = createOrg(userRepo, encoder, "vidaverde", "vidaverde@mail.com", "Vida Verde",
                "+598 99 100 002", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.CARTON, MaterialCategory.VIDRIO));
        // Perfil reducido a propósito: ya no acepta PLASTICO (ver solicitud #8 abajo).
        User papelAmigo = createOrg(userRepo, encoder, "papelamigo", "papelamigo@mail.com", "Papel Amigo",
                "+598 99 100 003", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.CARTON));

        User rosa = createUser(userRepo, encoder, "rosaperez", "rosaperez@mail.com", "Rosa Pérez", Role.USER, "+598 99 200 001", City.RIVERA);
        User mateo = createUser(userRepo, encoder, "mateosilva", "mateosilva@mail.com", "Mateo Silva", Role.USER, "+598 99 200 002", City.RIVERA);
        User carla = createUser(userRepo, encoder, "carlanunez", "carlanunez@mail.com", "Carla Núñez", Role.USER, "+598 99 200 003", City.RIVERA);
        User diego = createUser(userRepo, encoder, "diegoacosta", "diegoacosta@mail.com", "Diego Acosta", Role.USER, "+598 99 200 004", City.RIVERA);
        User joaoBr = createUser(userRepo, encoder, "joaosouza", "joaosouza@mail.com", "João Souza", Role.USER, "+55 55 200 005", City.LIVRAMENTO);

        LocalDateTime now = LocalDateTime.now();

        // 1. PENDING — plástico + cartón, barrio de Rivera. Para aceptar en vivo.
        createRequest(requestRepo, rosa, renacer, "Av. Sarandí 1450", "Barrio Centro", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.CARTON), RequestStatus.PENDING, null,
                null, null, null, now.minusHours(3));

        // 2. IN_PROGRESS — ya aceptada por Renacer. Para completar en vivo.
        createRequest(requestRepo, mateo, renacer, "Calle Uruguay 880", "Barrio Mandubí", City.RIVERA,
                List.of(MaterialCategory.VIDRIO), RequestStatus.IN_PROGRESS, TimeSlot.TARDE,
                null, null, null, now.minusDays(1));

        // 3. COMPLETED — fecha pasada, para historial/métricas.
        createRequest(requestRepo, carla, vidaVerde, "Calle Ceballos 320", "Barrio Cerro", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.COMPLETED, TimeSlot.MANANA,
                null, null, null, now.minusMonths(2));

        // 4. REJECTED — sin motivo (gap de modelo documentado arriba y en docs/DEFENSA.md).
        createRequest(requestRepo, diego, vidaVerde, "Calle Agraciada 610", "Barrio Mandubí", City.RIVERA,
                List.of(MaterialCategory.METAL), RequestStatus.REJECTED, null,
                null, null, null, now.minusDays(5));

        // 5. Escombros — ningún org de este dataset acepta ESCOMBROS; sin organización a
        // propósito (RequestService.validateMaterials lo bloquearía en la UI real).
        createRequest(requestRepo, rosa, null, "Camino Cuñapirú km 4", "Zona rural", City.RIVERA,
                List.of(MaterialCategory.ESCOMBROS), RequestStatus.PENDING, null,
                null, null, null, now.minusHours(6));

        // 6. Livramento — dirección del otro lado. Reusa un org del dataset genérico
        // (este dataset de defensa solo define orgs en Rivera).
        createRequest(requestRepo, joaoBr, orgLivramento, "Rua General Vasco Alves 210", "Frente à praça", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO), RequestStatus.PENDING, null,
                null, null, null, now.minusHours(12));

        // 7. Guest — sin cuenta, con código de rastreo real (el helper genérico no lo seteaba).
        createRequest(requestRepo, null, renacer, "Calle Treinta y Tres 95", "Barrio Roosevelt", City.RIVERA,
                List.of(MaterialCategory.PAPEL), RequestStatus.PENDING, null,
                "Beatriz Invitada", "+59899300001", "DEMO2026", now.minusHours(1));

        // 8. PENDING con plástico — asignada a Papel Amigo, que ya no acepta PLASTICO.
        // Trade-off documentado: accept() no revalida materiales (ver docs/DEFENSA.md §7).
        createRequest(requestRepo, mateo, papelAmigo, "Calle Anzani 500", "Barrio Lavalleja", City.RIVERA,
                List.of(MaterialCategory.PLASTICO), RequestStatus.PENDING, null,
                null, null, null, now.minusHours(8));

        // 9. Reasignada — PENDING, para editar/reasignar en vivo de un org a otro.
        createRequest(requestRepo, carla, vidaVerde, "Calle Suárez 210", "Barrio Mandubí", City.RIVERA,
                List.of(MaterialCategory.CARTON), RequestStatus.PENDING, null,
                null, null, null, now.minusHours(4));

        // 10. En portugués — ciudadano de Livramento pidiendo a un org de Livramento.
        createRequest(requestRepo, joaoBr, orgLivramento, "Rua Uruguai 88", "Bairro Centro", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL), RequestStatus.IN_PROGRESS, TimeSlot.NOCHE,
                null, null, null, now.minusDays(3));

        // ~22 COMPLETED de volumen (últimos 6 meses, sin criterio) para que el Kanban y
        // RequestMetricsService no se vean vacíos.
        String[] barrios = {"Centro", "Mandubí", "Cerro", "Lavalleja", "Roosevelt", "Villa Olímpica", "Cuñapirú", "20 de Setiembre"};
        List<List<MaterialCategory>> combos = List.of(
                List.of(MaterialCategory.PLASTICO), List.of(MaterialCategory.PAPEL, MaterialCategory.CARTON),
                List.of(MaterialCategory.VIDRIO), List.of(MaterialCategory.METAL),
                List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO), List.of(MaterialCategory.CARTON));
        User[] citizens = {rosa, mateo, carla, diego};
        User[] orgs = {renacer, vidaVerde, papelAmigo};
        java.util.Random rng = new java.util.Random(2026);
        for (int i = 0; i < 22; i++) {
            User citizen = citizens[rng.nextInt(citizens.length)];
            User org = orgs[rng.nextInt(orgs.length)];
            String barrio = barrios[rng.nextInt(barrios.length)];
            List<MaterialCategory> materials = combos.get(rng.nextInt(combos.size()));
            LocalDateTime createdAt = now.minusDays(1 + rng.nextInt(180));
            createRequest(requestRepo, citizen, org, "Calle " + barrio + " " + (100 + rng.nextInt(900)), null, City.RIVERA,
                    materials, RequestStatus.COMPLETED, TimeSlot.values()[rng.nextInt(TimeSlot.values().length)],
                    null, null, null, createdAt);
        }
    }

    private static User createUser(UserRepository repo, PasswordEncoder encoder,
                                   String username, String email, String firstName,
                                   Role role, String phone, City city) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode("1234"));
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
        createRequest(repo, user, org, address, ref, city, materials, status, slot, guestName, guestPhone, null, null);
    }

    /** Overload con trackingCode/createdAt explícitos (dataset de defensa) — los 2 overloads
     * de arriba siguen igual, delegan acá con null y conservan su comportamiento previo. */
    private static void createRequest(RequestRepository repo, User user, User org,
                                      String address, String ref, City city,
                                      List<MaterialCategory> materials, RequestStatus status, TimeSlot slot,
                                      String guestName, String guestPhone, String trackingCode, LocalDateTime createdAt) {
        Request r = new Request();
        r.setUser(user);
        r.setOrganization(org);
        r.setGuestName(guestName);
        r.setGuestPhone(guestPhone);
        r.setTrackingCode(trackingCode);
        r.setAddress(address);
        r.setAddressReference(ref);
        r.setCity(city);
        r.setMaterials(materials);
        r.restoreStatus(status);
        r.setConfirmedSlot(slot);
        r.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now().minusDays((long)(Math.random() * 10)));
        repo.save(r);
    }
}
