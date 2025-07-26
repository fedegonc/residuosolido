package com.residuosolido.app.service;

import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.repository.WasteSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WasteSectionService {
    
    @Autowired
    private WasteSectionRepository wasteSectionRepository;
    
    public List<WasteSection> findAll() {
        return wasteSectionRepository.findAll();
    }
    
    public List<WasteSection> getActiveSections() {
        return wasteSectionRepository.findByActiveOrderByDisplayOrderAsc(true);
    }
    
    public void initializeDefaultSections() {
        if (wasteSectionRepository.count() == 0) {
            wasteSectionRepository.save(new WasteSection(
                "Residuos Reciclables",
                "Plástico, metal, papel, vidrio",
                "🌱",
                "Ver puntos cercanos",
                "📍",
                1
            ));
            
            wasteSectionRepository.save(new WasteSection(
                "Residuos No Reciclables",
                "Escombros, podas, orgánicos",
                "🚫",
                "Ver horarios",
                "📅",
                2
            ));
            
            wasteSectionRepository.save(new WasteSection(
                "Gestión y Servicios",
                "Horarios, ubicaciones, normativas",
                "⚙️",
                "Contactar",
                "📞",
                3
            ));
        }
    }
}
