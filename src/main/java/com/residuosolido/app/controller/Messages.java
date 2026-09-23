package com.residuosolido.app.controller;

import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.Keyed;
import com.residuosolido.app.exception.ServerMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Mensajes server-side para la vista: resuelve claves i18n tipadas
 * ({@link ServerMessage}/{@link Keyed}), escribe flash attributes para
 * redirects y agrega los atributos compartidos del formulario de solicitud.
 *
 * Reemplaza a {@code BaseController}: los controllers y los handlers globales
 * lo inyectan por constructor en vez de heredarlo.
 */
@Component
public class Messages {

    private final MessageSource messageSource;

    public Messages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Traduce una clave i18n al idioma actual. */
    public String msg(ServerMessage key) {
        return messageSource.getMessage(key.code(), null, key.code(), LocaleContextHolder.getLocale());
    }

    /** Traduce una clave i18n con parámetros. */
    public String msg(ServerMessage key, Object... args) {
        return messageSource.getMessage(key.code(), args, key.code(), LocaleContextHolder.getLocale());
    }

    /**
     * Mensaje de una excepción: usa la clave tipada si la excepción la lleva
     * (Keyed), si no cae al getMessage() — que para excepciones de dominio
     * ya es la clave generada por el enum.
     */
    public String msg(Throwable e) {
        String code = e instanceof Keyed k ? k.key().code() : e.getMessage();
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }

    /** Mensaje de éxito flash para la próxima vista. */
    public void flashSuccess(RedirectAttributes ra, ServerMessage key) {
        ra.addFlashAttribute("successMessage", msg(key));
    }

    /** Mensaje de error flash para la próxima vista. */
    public void flashError(RedirectAttributes ra, ServerMessage key) {
        ra.addFlashAttribute("errorMessage", msg(key));
    }

    /** Mensaje de error flash desde una excepción (clave tipada o getMessage). */
    public void flashError(RedirectAttributes ra, Throwable e) {
        ra.addFlashAttribute("errorMessage", msg(e));
    }

    /** Mensaje de error flash con parámetros. */
    public void flashError(RedirectAttributes ra, ServerMessage key, Object... args) {
        ra.addFlashAttribute("errorMessage", msg(key, args));
    }

    /** Materiales y horarios al modelo para el formulario de solicitud. */
    public void addFormAttributes(Model model) {
        model.addAttribute("materials", MaterialCategory.values());
        model.addAttribute("timeSlots", TimeSlot.values());
    }
}
