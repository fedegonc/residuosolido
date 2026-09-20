package com.residuosolido.app.controller;

import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.Keyed;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Clase base para todos los controllers. Provee utilidades compartidas: usuario actual, i18n, flash messages. */
public abstract class BaseController {

    @Autowired
    protected UserService userService;

    @Autowired
    protected MessageSource messageSource;

    /** Obtiene el usuario autenticado actual desde la sesión. */
    protected User getCurrentUser(Authentication authentication) {
        return userService.findAuthenticatedUserByUsername(authentication.getName());
    }

    /** Traduce una clave i18n al idioma actual. */
    protected String msg(ServerMessage key) {
        return messageSource.getMessage(key.code(), null, key.code(), LocaleContextHolder.getLocale());
    }

    /** Traduce una clave i18n con parámetros. */
    protected String msg(ServerMessage key, Object... args) {
        return messageSource.getMessage(key.code(), args, key.code(), LocaleContextHolder.getLocale());
    }

    /**
     * Mensaje de una excepción: usa la clave tipada si la excepción la lleva
     * (Keyed), si no cae al getMessage() — que para excepciones de dominio
     * ya es la clave generada por el enum.
     */
    protected String msg(Throwable e) {
        String code = e instanceof Keyed k ? k.key().code() : e.getMessage();
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }

    /** Mensaje de éxito flash para la próxima vista. */
    protected void flashSuccess(RedirectAttributes ra, ServerMessage key) {
        ra.addFlashAttribute("successMessage", msg(key));
    }

    /** Mensaje de error flash para la próxima vista. */
    protected void flashError(RedirectAttributes ra, ServerMessage key) {
        ra.addFlashAttribute("errorMessage", msg(key));
    }

    /** Mensaje de error flash desde una excepción (clave tipada o getMessage). */
    protected void flashError(RedirectAttributes ra, Throwable e) {
        ra.addFlashAttribute("errorMessage", msg(e));
    }

    /** Mensaje de error flash con parámetros. */
    protected void flashError(RedirectAttributes ra, ServerMessage key, Object... args) {
        ra.addFlashAttribute("errorMessage", msg(key, args));
    }

    /** Agrega materiales y horarios al modelo para formularios de solicitud. */
    protected void addFormAttributes(Model model) {
        model.addAttribute("materials", MaterialCategory.values());
        model.addAttribute("timeSlots", TimeSlot.values());
    }
}
