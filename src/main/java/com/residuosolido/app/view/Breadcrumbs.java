package com.residuosolido.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.ui.Model;

/**
 * Helper object to carry breadcrumb data to Thymeleaf.
 * Usage:
 *   Breadcrumbs.forModule("Usuarios", "/admin/users")
 *              .title("Detalles")
 *              .apply(model);
 *
 * The breadcrumbs fragment expects two model attributes:
 *  - bcItems: List<BreadcrumbItem> (intermediate items after "Inicio")
 *  - currentTitle: String (last item, non-linked)
 */
public class Breadcrumbs {
    private final List<BreadcrumbItem> items = new ArrayList<>();
    private String currentTitle = "";

    private Breadcrumbs() {}

    public static Breadcrumbs empty() {
        return new Breadcrumbs();
    }

    public static Breadcrumbs forModule(String moduleLabel, String moduleUrl) {
        Breadcrumbs bc = new Breadcrumbs();
        if (moduleLabel != null && moduleUrl != null) {
            bc.items.add(new BreadcrumbItem(moduleLabel, moduleUrl));
        }
        return bc;
    }

    public Breadcrumbs addSection(String label, String url) {
        if (label != null) {
            items.add(new BreadcrumbItem(label, url));
        }
        return this;
    }

    public Breadcrumbs title(String title) {
        this.currentTitle = title != null ? title : "";
        return this;
    }

    public List<BreadcrumbItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    /**
     * Attach to Spring MVC model using the attributes expected by the fragment.
     */
    public void apply(Model model) {
        model.addAttribute("bcItems", items);
        model.addAttribute("currentTitle", currentTitle);
    }
}
