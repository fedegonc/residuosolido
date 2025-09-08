package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/feedback")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) Long id,
                       Model model) {
        List<Feedback> all = feedbackService.findAllOrderedByCreatedAtDesc();
        model.addAttribute("feedbacks", all);
        model.addAttribute("totalFeedback", all != null ? all.size() : 0);

        if (action != null) {
            switch (action) {
                case "view":
                    if (id != null) {
                        feedbackService.findById(id).ifPresent(feedback -> {
                            model.addAttribute("feedback", feedback);
                            model.addAttribute("viewType", "view");
                        });
                        return "admin/feedback";
                    }
                    break;
                case "edit":
                    if (id != null) {
                        feedbackService.findById(id).ifPresent(feedback -> {
                            model.addAttribute("feedback", feedback);
                            model.addAttribute("isEdit", true);
                            model.addAttribute("viewType", "form");
                        });
                        return "admin/feedback";
                    }
                    break;
                case "new":
                    Feedback newFeedback = new Feedback();
                    model.addAttribute("feedback", newFeedback);
                    model.addAttribute("isEdit", false);
                    model.addAttribute("viewType", "form");
                    return "admin/feedback";
            }
        }

        model.addAttribute("viewType", "list");
        return "admin/feedback";
    }

    @PostMapping
    public String save(@RequestParam(required = false) String action,
                       @ModelAttribute Feedback feedback,
                       RedirectAttributes ra) {
        
        if ("delete".equals(action) && feedback.getId() != null) {
            return delete(feedback.getId(), ra);
        }
        
        try {
            if (feedback.getId() != null) {
                // UPDATE: usar servicio directamente
                feedbackService.save(feedback);
                ra.addFlashAttribute("successMessage", "Feedback actualizado");
            } else {
                // CREATE: nuevo feedback
                feedbackService.save(feedback);
                ra.addFlashAttribute("successMessage", "Feedback creado");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/feedback";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            feedbackService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Feedback eliminado");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/feedback";
    }
}
