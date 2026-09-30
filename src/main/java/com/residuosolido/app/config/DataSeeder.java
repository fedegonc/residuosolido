package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de prueba (dev/test). Contiene el dataset completo que antes vivía en
 * DataLoader — genérico + curado para la defensa (ver seedDefenseDemoData y
 * docs/DEFENSA.md §Dataset de demo). DataLoader solo decide CUÁNDO sembrar.
 */
@Component
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      OrganizationRepository organizationRepository,
                      RequestRepository requestRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seedAllIfNeeded() {
        logger.info("=== Iniciando carga de datos de prueba ===");
        if (userRepository.count() > 0 || requestRepository.count() > 0) {
            logger.info("Seed omitido: la base ya contiene datos");
            return;
        }
        // Usuarios normales con PINs variados
        User u1 = createUser("juan", "juan@mail.com", "Juan Pérez", Role.USER, "+598 99 123 456", City.RIVERA, "5678");
        User u2 = createUser("maria", "maria@mail.com", "María García", Role.USER, "+55 55 987 654", City.LIVRAMENTO, "9012");
        User u3 = createUser("pedro", "pedro@mail.com", "Pedro Martínez", Role.USER, "+598 99 222 333", City.RIVERA, "3456");
        User u4 = createUser("lucia", "lucia@mail.com", "Lucía Fernández", Role.USER, "+55 55 111 222", City.LIVRAMENTO, "7890");
        // Organizaciones con PINs variados para testing realista
        Organization o1 = createOrg("coopverde", "coopverde@mail.com", "Cooperativa Verde", "+598 99 111 222", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.VIDRIO), "2468");
        Organization o2 = createOrg("reciclarivera", "reciclarivera@mail.com", "ReciclaRivera", "+598 99 333 444", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), "3579");
        Organization o3 = createOrg("ecofrontera", "ecofrontera@mail.com", "EcoFrontera", "+598 99 555 666", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO, MaterialCategory.METAL), "1357");
        Organization o4 = createOrg("reciclart", "reciclart@mail.com", "ReciclaRT", "+55 55 333 444", City.LIVRAMENTO,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.METAL), "8642");
        Organization o5 = createOrg("coopesperanca", "coopesperanca@mail.com", "Cooperativa Esperança", "+55 55 777 888", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO), "9753");
        Organization o6 = createOrg("verdefronteira", "verdefronteira@mail.com", "Verde Fronteira", "+55 55 999 000", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO, MaterialCategory.METAL, MaterialCategory.PLASTICO), "4682");

        createRequest(u1, o1, "Calle 18 de Julio 123", "Frente al supermercado", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL), RequestStatus.PENDING, null);
        createRequest(u2, o4, "Rua dos Andradas 456", "Esquina con farmacia", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO), RequestStatus.IN_PROGRESS, TimeSlot.MANANA);
        createRequest(u1, o1, "Av. Sarandí 789", "Portón verde", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.TARDE);
        createRequest(u2, null, "Rua Flores da Cunha 32", null, City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL), RequestStatus.PENDING, null);
        createRequest(u3, o2, "Calle Independencia 202", "Al lado de la plaza", City.RIVERA,
                List.of(MaterialCategory.METAL), RequestStatus.PENDING, null);
        createRequest(u4, o5, "Rua Marechal Deodoro 77", "Portão azul", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO), RequestStatus.IN_PROGRESS, TimeSlot.TARDE);
        createRequest(u3, o3, "Calle Rivera 450", "Frente a la escuela", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.COMPLETED, TimeSlot.MANANA);
        createRequest(u4, o6, "Rua Bento Gonçalves 890", "Galpão vermelho", City.LIVRAMENTO,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.METAL), RequestStatus.PENDING, null);
        createRequest(u1, o2, "Calle Amethyst 15", "Casa esquinera", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.NOCHE);
        createRequest(u2, o6, "Rua dos Imigrantes 120", "Frente ao mercado", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.REJECTED, null);

        seedDefenseDemoData(o4);

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
    private void seedDefenseDemoData(Organization orgLivramento) {
        Organization renacer = createOrg("renacer", "renacer@mail.com", "Cooperativa Renacer",
                "+598 99 100 001", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.CARTON, MaterialCategory.VIDRIO, MaterialCategory.METAL));
        Organization vidaVerde = createOrg("vidaverde", "vidaverde@mail.com", "Vida Verde",
                "+598 99 100 002", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.CARTON, MaterialCategory.VIDRIO));
        // Perfil reducido a propósito: ya no acepta PLASTICO (ver solicitud #8 abajo).
        Organization papelAmigo = createOrg("papelamigo", "papelamigo@mail.com", "Papel Amigo",
                "+598 99 100 003", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.CARTON));

        User rosa = createUser("rosaperez", "rosaperez@mail.com", "Rosa Pérez", Role.USER, "+598 99 200 001", City.RIVERA);
        User mateo = createUser("mateosilva", "mateosilva@mail.com", "Mateo Silva", Role.USER, "+598 99 200 002", City.RIVERA);
        User carla = createUser("carlanunez", "carlanunez@mail.com", "Carla Núñez", Role.USER, "+598 99 200 003", City.RIVERA);
        User diego = createUser("diegoacosta", "diegoacosta@mail.com", "Diego Acosta", Role.USER, "+598 99 200 004", City.RIVERA);
        User joaoBr = createUser("joaosouza", "joaosouza@mail.com", "João Souza", Role.USER, "+55 55 200 005", City.LIVRAMENTO);

        LocalDateTime now = LocalDateTime.now();

        // 1. PENDING — plástico + cartón, barrio de Rivera. Para aceptar en vivo.
        createRequest(rosa, renacer, "Av. Sarandí 1450", "Barrio Centro", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.CARTON), RequestStatus.PENDING, null,
                now.minusHours(3));

        // 2. IN_PROGRESS — ya aceptada por Renacer. Para completar en vivo.
        createRequest(mateo, renacer, "Calle Uruguay 880", "Barrio Mandubí", City.RIVERA,
                List.of(MaterialCategory.VIDRIO), RequestStatus.IN_PROGRESS, TimeSlot.TARDE, now.minusDays(1));

        // 3. COMPLETED — fecha pasada, para historial/métricas.
        createRequest(carla, vidaVerde, "Calle Ceballos 320", "Barrio Cerro", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.COMPLETED, TimeSlot.MANANA, now.minusMonths(2));

        // 4. REJECTED — sin motivo (gap de modelo documentado arriba y en docs/DEFENSA.md).
        createRequest(diego, vidaVerde, "Calle Agraciada 610", "Barrio Mandubí", City.RIVERA,
                List.of(MaterialCategory.METAL), RequestStatus.REJECTED, null, now.minusDays(5));

        // 5. Escombros — ningún org de este dataset acepta ESCOMBROS; sin organización a
        // propósito (RequestService.validateMaterials lo bloquearía en la UI real).
        createRequest(rosa, null, "Camino Cuñapirú km 4", "Zona rural", City.RIVERA,
                List.of(MaterialCategory.ESCOMBROS), RequestStatus.PENDING, null, now.minusHours(6));

        // 6. Livramento — dirección del otro lado. Reusa un org del dataset genérico
        // (este dataset de defensa solo define orgs en Rivera).
        createRequest(joaoBr, orgLivramento, "Rua General Vasco Alves 210", "Frente à praça", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO), RequestStatus.PENDING, null,
                now.minusHours(12));

        // 8. PENDING con plástico — asignada a Papel Amigo, que ya no acepta PLASTICO.
        // Trade-off documentado: accept() no revalida materiales (ver docs/DEFENSA.md §7).
        createRequest(mateo, papelAmigo, "Calle Anzani 500", "Barrio Lavalleja", City.RIVERA,
                List.of(MaterialCategory.PLASTICO), RequestStatus.PENDING, null, now.minusHours(8));

        // 9. Reasignada — PENDING, para editar/reasignar en vivo de un org a otro.
        createRequest(carla, vidaVerde, "Calle Suárez 210", "Barrio Mandubí", City.RIVERA,
                List.of(MaterialCategory.CARTON), RequestStatus.PENDING, null, now.minusHours(4));

        // 10. En portugués — ciudadano de Livramento pidiendo a un org de Livramento.
        createRequest(joaoBr, orgLivramento, "Rua Uruguai 88", "Bairro Centro", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL), RequestStatus.IN_PROGRESS, TimeSlot.NOCHE, now.minusDays(3));

        // ~22 COMPLETED de volumen (últimos 6 meses, sin criterio) para que el Kanban y
        // RequestMetricsService no se vean vacíos.
        String[] barrios = {"Centro", "Mandubí", "Cerro", "Lavalleja", "Roosevelt", "Villa Olímpica", "Cuñapirú", "20 de Setiembre"};
        List<List<MaterialCategory>> combos = List.of(
                List.of(MaterialCategory.PLASTICO), List.of(MaterialCategory.PAPEL, MaterialCategory.CARTON),
                List.of(MaterialCategory.VIDRIO), List.of(MaterialCategory.METAL),
                List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO), List.of(MaterialCategory.CARTON));
        User[] citizens = {rosa, mateo, carla, diego};
        Organization[] orgs = {renacer, vidaVerde, papelAmigo};
        java.util.Random rng = new java.util.Random(2026);
        for (int i = 0; i < 22; i++) {
            User citizen = citizens[rng.nextInt(citizens.length)];
            Organization org = orgs[rng.nextInt(orgs.length)];
            String barrio = barrios[rng.nextInt(barrios.length)];
            List<MaterialCategory> materials = combos.get(rng.nextInt(combos.size()));
            LocalDateTime createdAt = now.minusDays(1 + rng.nextInt(180));
            createRequest(citizen, org, "Calle " + barrio + " " + (100 + rng.nextInt(900)), null, City.RIVERA,
                    materials, RequestStatus.COMPLETED, TimeSlot.values()[rng.nextInt(TimeSlot.values().length)],
                    createdAt);
        }
    }

    private User createUser(String username, String email, String firstName,
                            Role role, String phone, City city, String pin) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pin));
        u.setRole(role);
        u.setFirstName(firstName);
        u.setPhone(UserValidator.canonicalPhone(phone));
        u.setCity(city);
        u.setActive(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private User createUser(String username, String email, String firstName,
                            Role role, String phone, City city) {
        return createUser(username, email, firstName, role, phone, city, "1234");
    }

    private Organization createOrg(String username, String email, String firstName,
                                   String phone, City city, List<MaterialCategory> acceptedMaterials, String pin) {
        User u = createUser(username, email, firstName, Role.ORGANIZATION, phone, city, pin);
        Organization org = new Organization();
        org.setId(u.getId());
        org.setName(firstName);
        org.setPhone(PhoneNumber.normalize(phone));
        org.setCity(city);
        org.setAcceptedMaterials(acceptedMaterials);
        org.setProfileCompleted(true);
        return organizationRepository.save(org);
    }

    private Organization createOrg(String username, String email, String firstName,
                                   String phone, City city, List<MaterialCategory> acceptedMaterials) {
        return createOrg(username, email, firstName, phone, city, acceptedMaterials, "1234");
    }

    private void createRequest(User user, Organization org,
                               String address, String ref, City city,
                               List<MaterialCategory> materials, RequestStatus status, TimeSlot slot) {
        createRequest(user, org, address, ref, city, materials, status, slot, null);
    }

    private void createRequest(User user, Organization org,
                               String address, String ref, City city,
                               List<MaterialCategory> materials, RequestStatus status, TimeSlot slot,
                               LocalDateTime createdAt) {
        if (user == null) {
            throw new IllegalArgumentException("Solo usuarios registrados pueden crear solicitudes");
        }
        Request r = new Request();
        r.setContactUser(user);
        if (org != null) r.assignOrganization(org);
        r.updateDraft(city, address, ref, materials);
        r.restoreStatus(status);
        r.setConfirmedSlot(slot);
        r.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now().minusDays((long)(Math.random() * 10)));
        requestRepository.save(r);
    }
}
