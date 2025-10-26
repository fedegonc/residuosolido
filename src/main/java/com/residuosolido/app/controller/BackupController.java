package com.residuosolido.app.controller;

import com.residuosolido.app.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Locale;

@Controller
@RequestMapping("/admin/backup")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class BackupController {

    private final BackupService backupService;

    /**
     * Descarga un backup completo de la base de datos en formato JSON
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadBackup() {
        try {
            log.info("Generando backup para descarga...");
            byte[] backupData = backupService.generateBackup();
            String filename = backupService.generateBackupFilename();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(backupData.length);
            
            log.info("Backup generado exitosamente: {}", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(backupData);
                    
        } catch (Exception e) {
            log.error("Error al generar backup", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Restaura la base de datos desde un archivo de backup JSON
     */
    @PostMapping("/restore")
    public String restoreBackup(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validaciones
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Por favor selecciona un archivo de backup");
                return "redirect:/admin/backup";
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".json")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "El archivo debe ser un backup JSON válido (.json)");
                return "redirect:/admin/backup";
            }
            
            // Validar tamaño (máximo 50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "El archivo es demasiado grande (máximo 50MB)");
                return "redirect:/admin/backup";
            }
            
            log.info("Iniciando restauración de backup desde archivo: {}", file.getOriginalFilename());
            backupService.restoreBackup(file);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Backup restaurado exitosamente. Todos los datos han sido recuperados.");
            log.info("Backup restaurado exitosamente");
            
        } catch (IOException e) {
            log.error("Error al leer archivo de backup", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error al leer el archivo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Archivo de backup inválido", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Archivo de backup inválido: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al restaurar backup", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error al restaurar el backup: " + e.getMessage());
        }
        
        return "redirect:/admin/backup";
    }

    /**
     * Muestra la página de gestión de backups
     */
    @GetMapping
    public String showBackupPage() {
        return "admin/backup";
    }
}
