package com.residuosolido.app.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DocumentationController {

    @GetMapping("/documentation")
    public String showDocumentation() {
        return "admin/documentation";
    }
}
