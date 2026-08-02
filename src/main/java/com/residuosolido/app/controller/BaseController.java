package com.residuosolido.app.controller;

import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class BaseController {

    @Autowired
    protected UserService userService;

    @Autowired
    protected MessageSource messageSource;

    protected User getCurrentUser(Authentication authentication) {
        return userService.findAuthenticatedUserByUsername(authentication.getName());
    }

    protected String msg(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    protected String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    protected void flashSuccess(RedirectAttributes ra, String key) {
        ra.addFlashAttribute("successMessage", msg(key));
    }

    protected void flashError(RedirectAttributes ra, String key) {
        ra.addFlashAttribute("errorMessage", msg(key));
    }

    protected void flashError(RedirectAttributes ra, String key, Object... args) {
        ra.addFlashAttribute("errorMessage", msg(key, args));
    }

    protected void addFormAttributes(Model model) {
        model.addAttribute("materials", MaterialCategory.values());
        model.addAttribute("timeSlots", TimeSlot.values());
    }
}
