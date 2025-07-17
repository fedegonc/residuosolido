package com.residuosolido.app.model;

/**
 * Enum que define los posibles estados de procesamiento de los materiales recolectados
 * Este es un ejemplo de una clase enum común que puede ser utilizada en múltiples entidades
 */
public enum ProcessingStatus {
    COLLECTED("Recolectado", "El material ha sido recolectado pero aún no ha sido procesado"),
    SORTED("Clasificado", "El material ha sido clasificado según su tipo"),
    PROCESSED("Procesado", "El material ha sido procesado para su reciclaje"),
    RECYCLED("Reciclado", "El material ha sido completamente reciclado"),
    DISCARDED("Descartado", "El material no pudo ser reciclado y fue descartado");
    
    private final String displayName;
    private final String description;
    
    ProcessingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica si el estado actual es final (no se puede cambiar a otro estado)
     * @return true si es un estado final, false en caso contrario
     */
    public boolean isFinalState() {
        return this == RECYCLED || this == DISCARDED;
    }
    
    /**
     * Obtiene el siguiente estado lógico en el flujo de procesamiento
     * @return el siguiente estado o null si es un estado final
     */
    public ProcessingStatus getNextStatus() {
        switch (this) {
            case COLLECTED:
                return SORTED;
            case SORTED:
                return PROCESSED;
            case PROCESSED:
                return RECYCLED;
            default:
                return null; // Estados finales no tienen siguiente
        }
    }
}
