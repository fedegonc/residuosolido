package com.residuosolido.app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<WasteSection> wasteSections = new ArrayList<>();

    // === Constructores ===
    public Category() {}

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<WasteSection> getWasteSections() { return wasteSections; }
    public void setWasteSections(List<WasteSection> wasteSections) { this.wasteSections = wasteSections; }

    // === equals & hashCode (clave para comparaciones, colecciones) ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // === toString (evita imprimir relaciones para prevenir ciclos infinitos) ===
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
