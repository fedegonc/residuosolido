package com.residuosolido.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIShowcaseController {

    @GetMapping("/ui-showcase")
    public String uiShowcase() {
        return "ui-showcase";
    }
}
