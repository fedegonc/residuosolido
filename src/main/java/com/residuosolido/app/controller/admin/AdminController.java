package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

// This controller is now deprecated - functionality moved to specialized controllers
// AdminDashboardController - Dashboard
// AdminUserController - User management  
// AdminContentController - Posts and categories
// AdminSystemController - Feedback, requests, config
