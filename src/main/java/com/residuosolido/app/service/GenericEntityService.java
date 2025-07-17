package com.residuosolido.app.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio genérico para operaciones CRUD básicas en cualquier entidad
 * @param <T> Tipo de entidad
 * @param <ID> Tipo del ID de la entidad
 */
public abstract class GenericEntityService<T, ID> {

    protected abstract JpaRepository<T, ID> getRepository();

    /**
     * Encuentra todas las entidades
     * @return Lista de entidades
     */
    public List<T> findAll() {
        return getRepository().findAll();
    }

    /**
     * Encuentra una entidad por su ID
     * @param id ID de la entidad
     * @return Entidad encontrada o empty si no existe
     */
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    /**
     * Guarda una entidad nueva o actualiza una existente
     * @param entity Entidad a guardar
     * @return Entidad guardada
     */
    @Transactional
    public T save(T entity) {
        return getRepository().save(entity);
    }

    /**
     * Elimina una entidad por su ID
     * @param id ID de la entidad a eliminar
     */
    @Transactional
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    /**
     * Verifica si existe una entidad con el ID dado
     * @param id ID a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }
}
