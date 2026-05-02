package com.kgboilers.controller.admin;

import com.kgboilers.config.properties.AdminProperties;
import com.kgboilers.model.admin.AdminQuoteDetail;
import com.kgboilers.model.admin.AdminQuoteFilters;
import com.kgboilers.service.admin.AdminQuoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminQuoteController {

    private final AdminQuoteService adminQuoteService;
    private final AdminProperties adminProperties;

    public AdminQuoteController(AdminQuoteService adminQuoteService, AdminProperties adminProperties) {
        this.adminQuoteService = adminQuoteService;
        this.adminProperties = adminProperties;
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/admin/quotes";
    }

    @GetMapping("/admin/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        model.addAttribute("adminConfigured", hasAdminCredentials());
        model.addAttribute("error", error != null ? "Incorrect username or password." : null);
        model.addAttribute("message", logout != null ? "You have been signed out." : null);
        return "admin/login";
    }

    @GetMapping("/admin/quotes")
    public String quotes(@RequestParam(value = "q", required = false) String query,
                         @RequestParam(value = "service", required = false) String serviceType,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                         @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                         Model model) {
        AdminQuoteFilters filters = new AdminQuoteFilters(query, serviceType, status, dateFrom, dateTo);
        model.addAttribute("quotes", adminQuoteService.findAll(filters));
        model.addAttribute("filters", filters);
        model.addAttribute("serviceOptions", adminQuoteService.serviceOptions());
        model.addAttribute("statusOptions", adminQuoteService.statusOptions());
        return "admin/quotes";
    }

    @GetMapping("/admin/quotes/{id}")
    public String quoteDetail(@PathVariable long id, Model model) {
        AdminQuoteDetail quote = adminQuoteService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + id));
        model.addAttribute("quote", quote);
        return "admin/quote-detail";
    }

    @PostMapping("/admin/quotes/{id}/status")
    public String updateStatus(@PathVariable long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        adminQuoteService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("message", "Status updated.");
        return "redirect:/admin/quotes/" + id;
    }

    @PostMapping("/admin/quotes/{id}/job-photos")
    public String uploadJobPhotos(@PathVariable long id,
                                  @RequestParam(value = "pictures", required = false) List<MultipartFile> pictures,
                                  RedirectAttributes redirectAttributes) {
        try {
            int uploadedCount = adminQuoteService.addJobPictures(id, pictures);
            redirectAttributes.addFlashAttribute("message", uploadedCount + " job photo" + (uploadedCount == 1 ? "" : "s") + " uploaded.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/quotes/" + id;
    }

    private boolean hasAdminCredentials() {
        return adminProperties.getUsername() != null && !adminProperties.getUsername().isBlank()
                && adminProperties.getPassword() != null && !adminProperties.getPassword().isBlank();
    }
}
