package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.WasteSection;
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
    private WasteSectionService wasteSectionService;
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> getIndexData() {
        Map<String, Object> data = new HashMap<>();
        
        List<Post> posts = postService.getFirst5Posts();
        List<User> organizations = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        List<WasteSection> wasteSections = wasteSectionService.getActiveSections();
        
        data.put("posts", posts);
        data.put("organizations", organizations);
        data.put("wasteSections", wasteSections);
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
        user.setRole(Role.USER);
        user.setActive(true);
        user.setPreferredLanguage("es");
        
        return userRepository.save(user);
    }
}
