package com.residuosolido.app.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GenericEntityService<T, ID> {
    
    // Servicio genérico simplificado para casos de uso core
    
    public List<T> findAll(JpaRepository<T, ID> repository) {
        return repository.findAll();
    }
    
    public Optional<T> findById(JpaRepository<T, ID> repository, ID id) {
        return repository.findById(id);
    }
    
    public T save(JpaRepository<T, ID> repository, T entity) {
        return repository.save(entity);
    }
    
    public void deleteById(JpaRepository<T, ID> repository, ID id) {
        repository.deleteById(id);
    }
}
