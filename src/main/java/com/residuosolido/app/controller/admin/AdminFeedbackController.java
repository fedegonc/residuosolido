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

        if ("edit".equals(action) && id != null) {
            Optional<Feedback> fb = feedbackService.findById(id);
            fb.ifPresent(feedback -> model.addAttribute("feedback", feedback));
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/feedback";
        }

        model.addAttribute("viewType", "list");
        return "admin/feedback";
    }

    @PostMapping
    public String save(@ModelAttribute Feedback feedback,
                       RedirectAttributes ra) {
        try {
            // Solo permitir editar campos básicos: name, email, comment
            if (feedback.getId() != null) {
                feedbackService.findById(feedback.getId()).ifPresent(existing -> {
                    existing.setName(feedback.getName());
                    existing.setEmail(feedback.getEmail());
                    existing.setComment(feedback.getComment());
                    feedbackService.save(existing);
                });
                ra.addFlashAttribute("successMessage", "Feedback actualizado");
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
