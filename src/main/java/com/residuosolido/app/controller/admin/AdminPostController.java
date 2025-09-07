package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    @Autowired
    private PostService postService;


    @GetMapping("/admin/posts")
    public String listPosts(@RequestParam(required = false) String action,
                           @RequestParam(required = false) Long id,
                           Model model) {
        if ("new".equals(action)) {
            postService.preparePostFormView(model, null, false);
        } else if ("edit".equals(action) && id != null) {
            postService.preparePostFormView(model, id, true);
        } else {
            postService.preparePostListView(model);
        }
        return "admin/posts";
    }

    @PostMapping("/admin/posts")
    public String savePost(@ModelAttribute Post post,
                          RedirectAttributes ra) {
        try {
            String message = postService.savePost(post);
            ra.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes ra) {
        try {
            postService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Post eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }
}
