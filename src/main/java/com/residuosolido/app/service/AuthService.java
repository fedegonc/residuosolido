package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> getIndexData() {
        Map<String, Object> data = new HashMap<>();
        
        // Solo datos necesarios para el index público
        List<Post> posts = postService.getRecentPosts(5);
        List<User> organizations = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        List<Category> categories = categoryService.getActiveCategoriesOrderedByDisplayOrder();
        
        data.put("posts", posts);
        data.put("organizations", organizations);
        data.put("categories", categories);
        data.put("heroImage", configService.getHeroImageUrl());
        
        return data;
    }

    public Map<String, Object> getInvitadosData() {
        Map<String, Object> data = new HashMap<>();
        
        List<User> organizations = userRepository.findByRole(Role.ORGANIZATION);
        data.put("organizations", organizations);
        
        return data;
    }

    public String validateUserRegistration(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "El nombre de usuario es obligatorio";
        }
        // No permitir espacios en el nombre de usuario
        if (user.getUsername().matches(".*\\s+.*")) {
            return "El nombre de usuario no puede contener espacios";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "El nombre de usuario ya existe";
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "El email ya está registrado";
        }
        
        return null; // Sin errores
    }

    public User registerUser(User user) {
        return registerUser(user, false);
    }
    
    public User registerUser(User user, boolean isOrganization) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Primer usuario registrado será ADMIN
        // Si marca el checkbox de organización será ORGANIZATION
        // Si no, será USER
        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(isOrganization ? Role.ORGANIZATION : Role.USER);
        }
        
        user.setActive(true);
        user.setPreferredLanguage("es");
        
        return userRepository.save(user);
    }
}
