package com.residuosolido.app.controller.guest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VisualSystemController {

    @GetMapping("/sistema-visual")
    public String visualSystemPage() {
        return "pages/sistema-visual";
    }

    @GetMapping("/grid-test")
    public String gridTestPage() {
        return "pages/grid-test";
    }
}
