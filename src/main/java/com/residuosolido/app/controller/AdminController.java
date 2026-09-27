package com.residuosolido.app.controller;

import com.residuosolido.app.config.DataLoader;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Profile("dev")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/seed")
    @ResponseBody
    public String seedData() {
        try {
            userRepository.deleteAll();
            organizationRepository.deleteAll();
            requestRepository.deleteAll();
            DataLoader.seedAll(userRepository, organizationRepository, requestRepository, passwordEncoder);
            long count = organizationRepository.count();
            return "✅ Base limpiada y datos sembrados correctamente. Ahora hay " + count + " organizaciones";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/org-count")
    @ResponseBody
    public String getOrgCount() {
        long count = organizationRepository.count();
        return "Organizaciones en BD: " + count;
    }

    @GetMapping("/export-test-data")
    @ResponseBody
    public java.util.Map<String, Object> exportTestData() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("users", userRepository.findAll());
        data.put("organizations", organizationRepository.findAll());
        data.put("requests", requestRepository.findAll());
        return data;
    }
}
