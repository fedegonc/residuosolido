package com.residuosolido.app.controller.admin;

import com.residuosolido.app.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final FeedbackService feedbackService;

    public AdminApiController(UserService userService, PostService postService,
                             CategoryService categoryService, FeedbackService feedbackService) {
        this.userService = userService;
        this.postService = postService;
        this.categoryService = categoryService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            // Estadísticas principales
            Map<String, Object> stats = Map.of(
                "totalUsers", userService.findAll().size(),
                "totalPosts", postService.getAllPosts().size(),
                "totalCategories", categoryService.getAllCategories().size(),
                "totalFeedbacks", feedbackService.findAll().size()
            );

            // Usuarios recientes (últimos 5)
            List<Map<String, Object>> recentUsers = userService.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map((user) -> {
                    Map<String, Object> userMap = Map.of(
                        "id", user.getId(),
                        "name", (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                               (user.getLastName() != null ? user.getLastName() : "").trim(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole().toString(),
                        "createdAt", user.getCreatedAt(),
                        "initials", (user.getFirstName() != null && !user.getFirstName().isEmpty())
                            ? user.getFirstName().substring(0, 1).toUpperCase()
                            : user.getUsername().substring(0, 1).toUpperCase()
                    );
                    return userMap;
                })
                .collect(Collectors.toList());

            // Publicaciones recientes (últimas 5)
            List<Map<String, Object>> recentPosts = postService.getAllPosts().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map((post) -> {
                    Map<String, Object> postMap = Map.of(
                        "id", post.getId(),
                        "title", post.getTitle(),
                        "content", post.getContent().length() > 100
                            ? post.getContent().substring(0, 100) + "..."
                            : post.getContent(),
                        "category", post.getCategory() != null ? post.getCategory().getName() : "Sin categoría",
                        "createdAt", post.getCreatedAt()
                    );
                    return postMap;
                })
                .collect(Collectors.toList());

            // Información del sistema
            Map<String, Object> systemInfo = Map.of(
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0",
                "environment", "production"
            );

            Map<String, Object> response = Map.of(
                "success", true,
                "data", Map.of(
                    "stats", stats,
                    "recentUsers", recentUsers,
                    "recentPosts", recentPosts,
                    "systemInfo", systemInfo
                ),
                "message", "Dashboard data retrieved successfully"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Error retrieving dashboard data: " + e.getMessage()));
        }
    }
}
