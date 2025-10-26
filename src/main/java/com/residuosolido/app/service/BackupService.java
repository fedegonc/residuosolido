package com.residuosolido.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final RequestRepository requestRepository;
    private final FeedbackRepository feedbackRepository;
    private final ConfigRepository configRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};
    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Integer>> LIST_OF_INTEGER_TYPE = new TypeReference<>() {};

    /**
     * Genera un backup completo de la base de datos en formato JSON
     */
    @Transactional(readOnly = true)
    public byte[] generateBackup() throws IOException {
        log.info("Iniciando generación de backup...");
        
        Map<String, Object> backupData = new LinkedHashMap<>();
        
        // Metadata del backup
        backupData.put("backupDate", LocalDateTime.now().toString());
        backupData.put("version", "1.0");
        
        // Exportar todas las entidades
        backupData.put("users", exportUsers());
        backupData.put("materials", materialRepository.findAll());
        backupData.put("categories", categoryRepository.findAll());
        backupData.put("posts", exportPosts());
        backupData.put("requests", exportRequests());
        backupData.put("feedbacks", exportFeedbacks());
        backupData.put("configs", configRepository.findAll());
        backupData.put("passwordResetRequests", passwordResetRequestRepository.findAll());
        
        // Convertir a JSON
        ObjectMapper mapper = createObjectMapper();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mapper.writeValue(outputStream, backupData);
        
        log.info("Backup generado exitosamente");
        return outputStream.toByteArray();
    }

    /**
     * Restaura los datos desde un archivo de backup JSON
     */
    @Transactional
    public void restoreBackup(MultipartFile file) throws IOException {
        log.info("Iniciando restauración de backup...");
        
        ObjectMapper mapper = createObjectMapper();
        Map<String, Object> backupData = mapper.readValue(file.getInputStream(), MAP_TYPE_REF);
        
        // Validar versión
        String version = (String) backupData.get("version");
        if (!"1.0".equals(version)) {
            throw new IllegalArgumentException("Versión de backup no compatible: " + version);
        }
        
        // Limpiar datos existentes (en orden inverso por dependencias)
        log.info("Limpiando datos existentes...");
        passwordResetRequestRepository.deleteAll();
        // No eliminar feedbacks ni requests directamente - se eliminan en cascada con users
        postRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll(); // Esto eliminará feedbacks y requests en cascada
        materialRepository.deleteAll();
        configRepository.deleteAll();
        
        // Restaurar datos (en orden por dependencias)
        log.info("Restaurando datos...");
        // Mapas de remapeo de IDs (backupId -> newId)
        Map<Long, Long> materialIdMap = new HashMap<>();
        Map<Long, Long> categoryIdMap = new HashMap<>();
        Map<Long, Long> userIdMap = new HashMap<>();
        Map<Long, Long> requestIdMap = new HashMap<>();

        restoreMaterials(backupData, mapper, materialIdMap);
        restoreCategories(backupData, mapper, categoryIdMap);
        restoreUsers(backupData, mapper, userIdMap, materialIdMap);
        restorePosts(backupData, mapper, categoryIdMap);
        restoreRequests(backupData, mapper, userIdMap, materialIdMap, requestIdMap);
        restoreFeedbacks(backupData, mapper, userIdMap);
        restoreConfigs(backupData, mapper);
        restorePasswordResetRequests(backupData, mapper);
        
        log.info("Backup restaurado exitosamente");
    }

    /**
     * Genera el nombre del archivo de backup
     */
    public String generateBackupFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "residuosolido_backup_" + timestamp + ".json";
    }

    // Métodos privados de exportación

    private List<Map<String, Object>> exportUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("password", user.getPassword());
            userData.put("role", user.getRole().name());
            userData.put("preferredLanguage", user.getPreferredLanguage());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("profileImage", user.getProfileImage());
            userData.put("phone", user.getPhone());
            userData.put("address", user.getAddress());
            userData.put("latitude", user.getLatitude());
            userData.put("longitude", user.getLongitude());
            userData.put("addressReferences", user.getAddressReferences());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("lastAccessAt", user.getLastAccessAt());
            userData.put("active", user.isActive());
            
            // IDs de materiales asociados
            List<Long> materialIds = user.getMaterials().stream()
                    .map(Material::getId)
                    .toList();
            userData.put("materialIds", materialIds);
            
            result.add(userData);
        }
        
        return result;
    }

    private List<Map<String, Object>> exportPosts() {
        List<Post> posts = postRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Post post : posts) {
            Map<String, Object> postData = new LinkedHashMap<>();
            postData.put("id", post.getId());
            postData.put("title", post.getTitle());
            postData.put("content", post.getContent());
            postData.put("imageUrl", post.getImageUrl());
            postData.put("createdAt", post.getCreatedAt());
            postData.put("updatedAt", post.getUpdatedAt());
            postData.put("categoryId", post.getCategory() != null ? post.getCategory().getId() : null);
            
            result.add(postData);
        }
        
        return result;
    }

    private List<Map<String, Object>> exportRequests() {
        List<Request> requests = requestRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Request request : requests) {
            Map<String, Object> requestData = new LinkedHashMap<>();
            requestData.put("id", request.getId());
            requestData.put("userId", request.getUser().getId());
            requestData.put("description", request.getDescription());
            requestData.put("collectionAddress", request.getCollectionAddress());
            requestData.put("scheduledDate", request.getScheduledDate());
            requestData.put("createdAt", request.getCreatedAt());
            requestData.put("updatedAt", request.getUpdatedAt());
            requestData.put("status", request.getStatus().name());
            requestData.put("notes", request.getNotes());
            requestData.put("imageUrl", request.getImageUrl());
            
            // IDs de materiales asociados
            List<Long> materialIds = request.getMaterials().stream()
                    .map(Material::getId)
                    .toList();
            requestData.put("materialIds", materialIds);
            
            result.add(requestData);
        }
        
        return result;
    }

    private List<Map<String, Object>> exportFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Feedback feedback : feedbacks) {
            Map<String, Object> feedbackData = new LinkedHashMap<>();
            feedbackData.put("id", feedback.getId());
            feedbackData.put("userId", feedback.getUser() != null ? feedback.getUser().getId() : null);
            feedbackData.put("name", feedback.getName());
            feedbackData.put("email", feedback.getEmail());
            feedbackData.put("comment", feedback.getComment());
            feedbackData.put("createdAt", feedback.getCreatedAt());
            feedbackData.put("adminResponse", feedback.getAdminResponse());
            feedbackData.put("respondedAt", feedback.getRespondedAt());
            feedbackData.put("isRead", feedback.getIsRead());
            
            result.add(feedbackData);
        }
        
        return result;
    }

    // Métodos privados de restauración

    private void restoreMaterials(Map<String, Object> backupData, ObjectMapper mapper, Map<Long, Long> materialIdMap) {
        List<Map<String, Object>> materials = readMapList(mapper, backupData, "materials");
        if (!materials.isEmpty()) {
            for (Map<String, Object> materialData : materials) {
                Long oldId = materialData.get("id") != null ? ((Number) materialData.get("id")).longValue() : null;
                Material material = mapper.convertValue(materialData, Material.class);
                material.setId(null);
                Material saved = materialRepository.save(material);
                if (oldId != null) {
                    materialIdMap.put(oldId, saved.getId());
                }
            }
        }
    }

    private void restoreCategories(Map<String, Object> backupData, ObjectMapper mapper, Map<Long, Long> categoryIdMap) {
        List<Map<String, Object>> categories = readMapList(mapper, backupData, "categories");
        if (!categories.isEmpty()) {
            for (Map<String, Object> categoryData : categories) {
                // Tomar el ID original y evitar reutilizarlo
                Long oldId = categoryData.get("id") != null ? ((Number) categoryData.get("id")).longValue() : null;
                Category incoming = mapper.convertValue(categoryData, Category.class);
                String name = incoming.getName();
                if (name == null || name.isBlank()) {
                    log.warn("Omitiendo Category sin nombre. Datos: {}", categoryData);
                    continue;
                }
                // Upsert por nombre único
                Category target = categoryRepository.findByName(name).orElse(null);
                if (target == null) {
                    incoming.setId(null);
                    target = categoryRepository.save(incoming);
                } else {
                    // Actualizar campos relevantes sin cambiar el ID existente
                    target.setDescription(incoming.getDescription());
                    target.setImageUrl(incoming.getImageUrl());
                    target.setDisplayOrder(incoming.getDisplayOrder());
                    target.setActive(incoming.getActive());
                    target = categoryRepository.save(target);
                }
                if (oldId != null) {
                    categoryIdMap.put(oldId, target.getId());
                }
            }
        }
    }

    private void restoreUsers(Map<String, Object> backupData, ObjectMapper mapper, Map<Long, Long> userIdMap, Map<Long, Long> materialIdMap) {
        List<Map<String, Object>> users = readMapList(mapper, backupData, "users");
        if (!users.isEmpty()) {
            for (Map<String, Object> userData : users) {
                User user = new User();
                Long oldId = ((Number) userData.get("id")).longValue();
                user.setUsername((String) userData.get("username"));
                user.setEmail((String) userData.get("email"));
                user.setPassword((String) userData.get("password"));
                user.setRole(Role.valueOf((String) userData.get("role")));
                user.setPreferredLanguage((String) userData.get("preferredLanguage"));
                user.setFirstName((String) userData.get("firstName"));
                user.setLastName((String) userData.get("lastName"));
                user.setProfileImage((String) userData.get("profileImage"));
                user.setPhone((String) userData.get("phone"));
                user.setAddress((String) userData.get("address"));
                
                if (userData.get("latitude") != null) {
                    user.setLatitude(new java.math.BigDecimal(userData.get("latitude").toString()));
                }
                if (userData.get("longitude") != null) {
                    user.setLongitude(new java.math.BigDecimal(userData.get("longitude").toString()));
                }
                
                user.setAddressReferences((String) userData.get("addressReferences"));
                user.setActive((Boolean) userData.get("active"));
                
                // Restaurar timestamps
                if (userData.get("createdAt") != null) {
                    user.setCreatedAt(LocalDateTime.parse(userData.get("createdAt").toString()));
                }
                if (userData.get("lastAccessAt") != null) {
                    user.setLastAccessAt(LocalDateTime.parse(userData.get("lastAccessAt").toString()));
                }
                
                user.setId(null);
                User savedUser = userRepository.save(user);
                userIdMap.put(oldId, savedUser.getId());
                
                // Restaurar relación con materiales
                List<Integer> materialIds = readIntegerList(mapper, userData.get("materialIds"));
                if (!materialIds.isEmpty()) {
                    List<Long> newIds = materialIds.stream()
                            .map(Integer::longValue)
                            .map(mid -> materialIdMap.getOrDefault(mid, mid))
                            .toList();
                    List<Material> materials = materialRepository.findAllById(newIds);
                    savedUser.setMaterials(materials);
                    userRepository.save(savedUser);
                }
            }
        }
    }

    private void restorePosts(Map<String, Object> backupData, ObjectMapper mapper, Map<Long, Long> categoryIdMap) {
        List<Map<String, Object>> posts = readMapList(mapper, backupData, "posts");
        if (!posts.isEmpty()) {
            for (Map<String, Object> postData : posts) {
                Post post = new Post();
                // No reutilizar ID
                post.setTitle((String) postData.get("title"));
                post.setContent((String) postData.get("content"));
                post.setImageUrl((String) postData.get("imageUrl"));
                
                if (postData.get("createdAt") != null) {
                    post.setCreatedAt(LocalDateTime.parse(postData.get("createdAt").toString()));
                }
                if (postData.get("updatedAt") != null) {
                    post.setUpdatedAt(LocalDateTime.parse(postData.get("updatedAt").toString()));
                }
                
                if (postData.get("categoryId") != null) {
                    Long oldCategoryId = ((Number) postData.get("categoryId")).longValue();
                    Long newCategoryId = categoryIdMap.getOrDefault(oldCategoryId, oldCategoryId);
                    categoryRepository.findById(newCategoryId).ifPresent(post::setCategory);
                }
                
                post.setId(null);
                postRepository.save(post);
            }
        }
    }

    private void restoreRequests(Map<String, Object> backupData, ObjectMapper mapper, Map<Long, Long> userIdMap, Map<Long, Long> materialIdMap, Map<Long, Long> requestIdMap) {
        List<Map<String, Object>> requests = readMapList(mapper, backupData, "requests");
        if (!requests.isEmpty()) {
            for (Map<String, Object> requestData : requests) {
                Request request = new Request();
                Long oldRequestId = ((Number) requestData.get("id")).longValue();
                request.setDescription((String) requestData.get("description"));
                request.setCollectionAddress((String) requestData.get("collectionAddress"));
                request.setStatus(RequestStatus.valueOf((String) requestData.get("status")));
                request.setNotes((String) requestData.get("notes"));
                request.setImageUrl((String) requestData.get("imageUrl"));
                
                if (requestData.get("scheduledDate") != null) {
                    request.setScheduledDate(java.time.LocalDate.parse(requestData.get("scheduledDate").toString()));
                }
                if (requestData.get("createdAt") != null) {
                    request.setCreatedAt(LocalDateTime.parse(requestData.get("createdAt").toString()));
                }
                if (requestData.get("updatedAt") != null) {
                    request.setUpdatedAt(LocalDateTime.parse(requestData.get("updatedAt").toString()));
                }
                
                // Relación con usuario (obligatoria). Si no existe, saltar esta request
                Long oldUserId = ((Number) requestData.get("userId")).longValue();
                Long newUserId = userIdMap.getOrDefault(oldUserId, oldUserId);
                Optional<User> reqUserOpt = userRepository.findById(newUserId);
                if (reqUserOpt.isEmpty()) {
                    log.warn("Omitiendo Request id={} (backup) porque no existe User id={} (mapeado desde {}).", oldRequestId, newUserId, oldUserId);
                    continue;
                }
                request.setUser(reqUserOpt.get());

                request.setId(null);
                Request savedRequest = requestRepository.save(request);
                requestIdMap.put(oldRequestId, savedRequest.getId());
                
                // Restaurar relación con materiales
                List<Integer> materialIds = readIntegerList(mapper, requestData.get("materialIds"));
                if (!materialIds.isEmpty()) {
                    List<Long> newIds = materialIds.stream()
                            .map(Integer::longValue)
                            .map(mid -> materialIdMap.getOrDefault(mid, mid))
                            .toList();
                    List<Material> materials = materialRepository.findAllById(newIds);
                    savedRequest.setMaterials(materials);
                    requestRepository.save(savedRequest);
                }
            }
        }
    }

    private void restoreFeedbacks(Map<String, Object> backupData, ObjectMapper mapper, Map<Long, Long> userIdMap) {
        List<Map<String, Object>> feedbacks = readMapList(mapper, backupData, "feedbacks");
        if (!feedbacks.isEmpty()) {
            for (Map<String, Object> feedbackData : feedbacks) {
                Feedback feedback = new Feedback();
                // No reutilizar ID
                feedback.setName((String) feedbackData.get("name"));
                feedback.setEmail((String) feedbackData.get("email"));
                feedback.setComment((String) feedbackData.get("comment"));
                feedback.setAdminResponse((String) feedbackData.get("adminResponse"));
                feedback.setIsRead((Boolean) feedbackData.get("isRead"));
                
                if (feedbackData.get("createdAt") != null) {
                    feedback.setCreatedAt(LocalDateTime.parse(feedbackData.get("createdAt").toString()));
                }
                
                if (feedbackData.get("respondedAt") != null) {
                    feedback.setRespondedAt(LocalDateTime.parse(feedbackData.get("respondedAt").toString()));
                }
                
                if (feedbackData.get("userId") != null) {
                    Long oldUserId = ((Number) feedbackData.get("userId")).longValue();
                    Long newUserId = userIdMap.getOrDefault(oldUserId, oldUserId);
                    Optional<User> fbUserOpt = userRepository.findById(newUserId);
                    if (fbUserOpt.isEmpty()) {
                        log.warn("Omitiendo Feedback id={} (backup) porque no existe User id={} (mapeado desde {}).", feedbackData.get("id"), newUserId, oldUserId);
                        continue;
                    }
                    feedback.setUser(fbUserOpt.get());
                } else {
                    log.warn("Omitiendo Feedback id={} (backup) porque no tiene userId.", feedbackData.get("id"));
                    continue;
                }

                feedback.setId(null);
                feedbackRepository.save(feedback);
            }
        }
    }

    private void restoreConfigs(Map<String, Object> backupData, ObjectMapper mapper) {
        List<Map<String, Object>> configs = readMapList(mapper, backupData, "configs");
        if (!configs.isEmpty()) {
            for (Map<String, Object> configData : configs) {
                Config config = mapper.convertValue(configData, Config.class);
                configRepository.save(config);
            }
        }
    }

    private void restorePasswordResetRequests(Map<String, Object> backupData, ObjectMapper mapper) {
        List<Map<String, Object>> requests = readMapList(mapper, backupData, "passwordResetRequests");
        if (!requests.isEmpty()) {
            for (Map<String, Object> requestData : requests) {
                PasswordResetRequest request = mapper.convertValue(requestData, PasswordResetRequest.class);
                passwordResetRequestRepository.save(request);
            }
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ignorar propiedades desconocidas para compatibilidad con backups antiguos
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private List<Map<String, Object>> readMapList(ObjectMapper mapper, Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return Collections.emptyList();
        }
        return mapper.convertValue(value, LIST_OF_MAP_TYPE);
    }

    private List<Integer> readIntegerList(ObjectMapper mapper, Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return mapper.convertValue(value, LIST_OF_INTEGER_TYPE);
    }
}
