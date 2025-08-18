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
        
        List<Post> posts = postService.getFirst5Posts();
        List<User> organizations = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        List<Category> categories = categoryService.getActiveCategoriesOrderedByDisplayOrder();
        List<User> users = userRepository.findAll();
        
        data.put("posts", posts);
        data.put("organizations", organizations);
        data.put("categories", categories);
        data.put("heroImage", configService.getHeroImageUrl());
        data.put("users", users);
        
        return data;
    }

    public Map<String, Object> getInvitadosData() {
        Map<String, Object> data = new HashMap<>();
        
        List<User> organizations = userRepository.findByRole(Role.ORGANIZATION);
        data.put("organizations", organizations);
        
        return data;
    }

    public String validateUserRegistration(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "El nombre de usuario ya existe";
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "El email ya está registrado";
        }
        
        return null; // Sin errores
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Primer usuario registrado será ADMIN, el resto USER
        long totalUsers = userRepository.count();
        user.setRole(totalUsers == 0 ? Role.ADMIN : Role.USER);
        user.setActive(true);
        user.setPreferredLanguage("es");
        
        return userRepository.save(user);
    }
}
