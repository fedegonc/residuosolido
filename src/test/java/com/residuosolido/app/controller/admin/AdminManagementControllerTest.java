package com.residuosolido.app.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminManagementControllerTest {

    private User testUser;
    private Post testPost;
    private Category testCategory;
    private Material testMaterial;
    private Request testRequest;
    private Feedback testFeedback;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testMaterial = new Material();
        testMaterial.setId(1L);
        testMaterial.setName("Test Material");

        testRequest = new Request();
        testRequest.setId(1L);
        testRequest.setDescription("Test Request");

        testFeedback = new Feedback();
        testFeedback.setId(1L);
        testFeedback.setMessage("Test Feedback");

        // Setup CrudConfig mock
        setupCrudConfigMocks();
    }

    private void setupCrudConfigMocks() {
        CrudConfig.EntityConfig userConfig = new CrudConfig.EntityConfig("users", "Usuarios", "Gestión de usuarios");
        CrudConfig.EntityConfig postConfig = new CrudConfig.EntityConfig("posts", "Posts", "Gestión de posts");
        CrudConfig.EntityConfig categoryConfig = new CrudConfig.EntityConfig("categories", "Categorías", "Gestión de categorías");
        CrudConfig.EntityConfig materialConfig = new CrudConfig.EntityConfig("materials", "Materiales", "Gestión de materiales");
        CrudConfig.EntityConfig requestConfig = new CrudConfig.EntityConfig("requests", "Solicitudes", "Gestión de solicitudes");
        CrudConfig.EntityConfig feedbackConfig = new CrudConfig.EntityConfig("feedback", "Feedback", "Gestión de feedback");

        when(crudConfig.getConfig("users")).thenReturn(userConfig);
        when(crudConfig.getConfig("posts")).thenReturn(postConfig);
        when(crudConfig.getConfig("categories")).thenReturn(categoryConfig);
        when(crudConfig.getConfig("materials")).thenReturn(materialConfig);
        when(crudConfig.getConfig("requests")).thenReturn(requestConfig);
        when(crudConfig.getConfig("feedback")).thenReturn(feedbackConfig);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsers() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);
        when(userService.count()).thenReturn(1L);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "list"))
                .andExpect(model().attribute("entity", "users"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("config"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListPosts() throws Exception {
        List<Post> posts = Arrays.asList(testPost);
        when(postService.findAll()).thenReturn(posts);
        when(postService.count()).thenReturn(1L);

        mockMvc.perform(get("/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "list"))
                .andExpect(model().attribute("entity", "posts"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListCategories() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.findAll()).thenReturn(categories);
        when(categoryService.count()).thenReturn(1L);

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "list"))
                .andExpect(model().attribute("entity", "categories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListMaterials() throws Exception {
        List<Material> materials = Arrays.asList(testMaterial);
        when(materialService.findAll()).thenReturn(materials);
        when(materialService.count()).thenReturn(1L);

        mockMvc.perform(get("/admin/materials"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "list"))
                .andExpect(model().attribute("entity", "materials"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListRequests() throws Exception {
        List<Request> requests = Arrays.asList(testRequest);
        when(requestService.findAll()).thenReturn(requests);
        when(requestService.count()).thenReturn(1L);

        mockMvc.perform(get("/admin/requests"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "list"))
                .andExpect(model().attribute("entity", "requests"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListFeedback() throws Exception {
        List<Feedback> feedbackList = Arrays.asList(testFeedback);
        when(feedbackService.findAll()).thenReturn(feedbackList);
        when(feedbackService.count()).thenReturn(1L);

        mockMvc.perform(get("/admin/feedback"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "list"))
                .andExpect(model().attribute("entity", "feedback"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testNewUserForm() throws Exception {
        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "form"))
                .andExpect(model().attribute("entity", "users"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditUserForm() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/admin/users/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "form"))
                .andExpect(model().attribute("entity", "users"))
                .andExpect(model().attribute("item", testUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewUser() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/crud"))
                .andExpect(model().attribute("viewType", "view"))
                .andExpect(model().attribute("entity", "users"))
                .andExpect(model().attribute("item", testUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/users/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testInvalidEntity() throws Exception {
        when(crudConfig.getConfig("invalid")).thenReturn(null);

        mockMvc.perform(get("/admin/invalid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }
}
