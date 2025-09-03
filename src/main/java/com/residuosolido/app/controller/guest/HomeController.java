package com.residuosolido.app.controller.guest;

import com.residuosolido.app.service.ConfigService;
import com.residuosolido.app.config.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final ConfigService configService;
    private final LoginSuccessHandler successHandler;

    @Autowired
    public HomeController(ConfigService configService, LoginSuccessHandler successHandler) {
        this.configService = configService;
        this.successHandler = successHandler;
    }

    @GetMapping("/")
    public String home(
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model) throws Exception {

        // Si el usuario está autenticado, redirigir al dashboard compartido
        if (userDetails != null) {
            successHandler.redirectToDashboard(request, response, userDetails);
            return null;
        }

        // Usuario no autenticado: mostrar página principal optimizada
        model.addAttribute("heroImage", configService.getHeroImageUrl());
        return "public/index";
    }

    @GetMapping("/home")
    public String homeAlias(
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model) throws Exception {
        return home(userDetails, request, response, model);
    }
}
