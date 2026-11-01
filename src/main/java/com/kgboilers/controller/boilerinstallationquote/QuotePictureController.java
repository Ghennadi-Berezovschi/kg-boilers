package com.kgboilers.controller.boilerinstallationquote;

import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Customer uploads are private to the uploading session and administrators. */
@Controller
public class QuotePictureController {
    private static final Pattern STORED_FILENAME = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)");
    private final Path uploadDirectory;

    public QuotePictureController(@Value("${kg.uploads.quote-pictures-dir}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @GetMapping("/uploads/quote-pictures/{filename}")
    public ResponseEntity<Resource> picture(@PathVariable String filename, HttpServletRequest request,
                                            Authentication authentication) throws IOException {
        if (!STORED_FILENAME.matcher(filename).matches()
                || !(isAdmin(authentication) || isOwner(request.getSession(false), filename))) {
            return missing();
        }
        Path picture = uploadDirectory.resolve(filename);
        if (!Files.isRegularFile(picture)
                || !picture.toRealPath().startsWith(uploadDirectory.toRealPath())) {
            return missing();
        }
        MediaType type = filename.endsWith(".png") ? MediaType.IMAGE_PNG
                : filename.endsWith(".webp") ? MediaType.parseMediaType("image/webp") : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.VARY, "Cookie")
                .contentType(type).body(new FileSystemResource(picture));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isOwner(HttpSession session, String filename) {
        if (session == null || !(session.getAttribute(QuoteSessionService.QUOTE_SESSION_STATE_KEY)
                instanceof QuoteSessionState state) || state.getUploadedPictures() == null) {
            return false;
        }
        return state.getUploadedPictures().stream()
                .anyMatch(picture -> picture != null && filename.equals(picture.getStoredFilename()));
    }

    private ResponseEntity<Resource> missing() {
        return ResponseEntity.status(404).cacheControl(CacheControl.noStore()).build();
    }
}
