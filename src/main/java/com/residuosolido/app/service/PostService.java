package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final List<Post> posts = new ArrayList<>();
    private Long nextId = 1L;

    public PostService() {
        // Posts para categoría "Reciclable" (ID: 1)
        posts.add(new Post(nextId++, "Guía de Reciclaje de Plásticos", 
            "Los plásticos son uno de los materiales más comunes en nuestros hogares. Aprende cómo identificar los diferentes tipos de plásticos y cuáles pueden ser reciclados en tu comunidad. El reciclaje adecuado de plásticos ayuda a reducir la contaminación y conservar recursos naturales.", 
            "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=400&h=200&fit=crop", 1L,
            "https://www.greenpeace.org/usa/research/plastic-recycling/", "Greenpeace"));
            
        posts.add(new Post(nextId++, "Reciclaje de Papel y Cartón", 
            "El papel y cartón representan una gran parte de nuestros residuos domésticos. Descubre las mejores prácticas para preparar estos materiales para el reciclaje, qué tipos se aceptan y cómo contribuir a la economía circular del papel.", 
            "https://images.unsplash.com/photo-1594736797933-d0401ba2fe65?w=400&h=200&fit=crop", 1L,
            "https://www.epa.gov/recycle/how-do-i-recycle-common-recyclables", "EPA"));
            
        // Posts para categoría "No Reciclable" (ID: 2)
        posts.add(new Post(nextId++, "Manejo de Residuos Peligrosos", 
            "Los residuos peligrosos como baterías, productos químicos y electrónicos requieren un manejo especial. Conoce los puntos de recolección especializados en tu área y las precauciones necesarias para su disposición segura.", 
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=200&fit=crop", 2L,
            "https://www.epa.gov/hw/household-hazardous-waste-hhw", "EPA"));
            
        // Posts para categoría "Informaciones" (ID: 3)
        posts.add(new Post(nextId++, "Educación Ambiental en el Hogar", 
            "La educación ambiental comienza en casa. Aprende estrategias efectivas para enseñar a toda la familia sobre la importancia de la gestión responsable de residuos y cómo hacer cambios positivos en nuestros hábitos diarios.", 
            "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=400&h=200&fit=crop", 3L,
            "https://www.unep.org/explore-topics/education-environment", "UNEP"));
            
        posts.add(new Post(nextId++, "Impacto Ambiental de los Residuos", 
            "Comprende el verdadero impacto que nuestros residuos tienen en el medio ambiente. Desde la contaminación del agua hasta el cambio climático, descubre cómo nuestras decisiones diarias afectan el planeta y qué podemos hacer al respecto.", 
            "https://images.unsplash.com/photo-1569163139394-de4e4f43e4e3?w=400&h=200&fit=crop", 3L,
            "https://www.worldbank.org/en/topic/urbandevelopment/brief/solid-waste-management", "World Bank"));
    }

    public List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    public List<Post> getFirst5Posts() {
        return posts.stream().limit(5).collect(java.util.stream.Collectors.toList());
    }

    public boolean hasMoreThan5Posts() {
        return posts.size() > 5;
    }

    // Reescrito para resolver el problema de método duplicado
    public List<Post> getPostsByCategoryId(Long categoryId) {
        return posts.stream()
                .filter(post -> post.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }

    // Reescrito para resolver el problema de método duplicado
    public List<Post> getRelatedPostsById(Long postId, Long categoryId, int limit) {
        return posts.stream()
                .filter(p -> p.getCategoryId().equals(categoryId) && !p.getId().equals(postId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void createPost(String title, String content, String imageUrl, Long categoryId) {
        posts.add(new Post(nextId++, title, content, imageUrl, categoryId));
    }
    
    public void createPost(String title, String content, String imageUrl, Long categoryId, String sourceUrl, String sourceName) {
        posts.add(new Post(nextId++, title, content, imageUrl, categoryId, sourceUrl, sourceName));
    }

    public Optional<Post> getPostById(Long id) {
        return posts.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public void updatePost(Long id, String title, String content, String imageUrl, Long categoryId) {
        getPostById(id).ifPresent(post -> {
            post.setTitle(title);
            post.setContent(content);
            post.setImageUrl(imageUrl);
            post.setCategoryId(categoryId);
        });
    }

    public void deletePost(Long id) {
        posts.removeIf(p -> p.getId().equals(id));
    }

    public boolean isCategoryInUse(Long categoryId) {
        return posts.stream().anyMatch(p -> p.getCategoryId().equals(categoryId));
    }
}
