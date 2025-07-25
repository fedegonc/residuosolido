package com.residuosolido.app.service;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.CategoryRepository;
import com.residuosolido.app.repository.FeedbackRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final PostService postService;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final FeedbackRepository feedbackRepository;
    private final RequestRepository requestRepository;

    @Autowired
    public DashboardService(UserRepository userRepository, PostService postService, CategoryRepository categoryRepository, CategoryService categoryService, FeedbackRepository feedbackRepository, RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.postService = postService;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.feedbackRepository = feedbackRepository;
        this.requestRepository = requestRepository;
    }

    public Map<String, Object> getGeneralStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPosts", postService.getAllPosts().size());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalOrganizations", userRepository.countByRole(Role.ORGANIZATION));
        stats.put("totalRequests", requestRepository.count());
        stats.put("totalFeedbacks", feedbackRepository.count());
        return stats;
    }

    public Map<String, Object> getAdminStats() {
        return getGeneralStats();
    }

    public Map<String, Object> getOrganizationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrganizations", userRepository.countByRole(Role.ORGANIZATION));
        stats.put("totalUsers", userRepository.countByRole(Role.USER));
        stats.put("totalRequests", requestRepository.count());
        return stats;
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postService.getAllPosts().size());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalOrganizations", userRepository.countByRole(Role.ORGANIZATION));
        return stats;
    }

    public Map<String, Object> getPublicPageData() {
        Map<String, Object> data = new HashMap<>();
        data.put("posts", postService.getFirst5Posts());
        data.put("hasMorePosts", postService.hasMoreThan5Posts());
        data.put("categories", categoryService.getCategoriesWithSlugs());
        data.put("heroImage", "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80");
        data.put("organizationCount", userRepository.countByRole(Role.ORGANIZATION));
        return data;
    }

}
