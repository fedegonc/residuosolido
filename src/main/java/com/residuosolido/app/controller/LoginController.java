package com.residuosolido.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/entrar")
    public String showLoginForm() {
        return "auth/login";
    }
}
