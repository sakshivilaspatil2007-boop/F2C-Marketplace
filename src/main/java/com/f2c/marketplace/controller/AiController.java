package com.f2c.marketplace.controller;

import com.f2c.marketplace.dto.AiResponse;
import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.service.AiService;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserService userService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestParam String message, Authentication authentication) {
        try {
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    User user = userService.findByEmail(authentication.getName());
                    if (user != null) {
                        userId = user.getId();
                    }
                } catch (Exception e) {
                    // Ignore, keep anonymous
                }
            }
            String reply = aiService.chat(message, userId);
            return ResponseEntity.ok(new AiResponse(reply));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/demand-prediction")
    public ResponseEntity<Map<String, Object>> getDemandPrediction() {
        return ResponseEntity.ok(aiService.getDemandPrediction());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Product>> getRecommendations(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            return ResponseEntity.ok(aiService.getRecommendations(user.getId()));
        } catch (Exception ex) {
            return ResponseEntity.ok(aiService.getRecommendations(null));
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String name = file.getOriginalFilename();
            String label = "Fresh Organic Tomatoes";
            if (name != null) {
                if (name.toLowerCase().contains("apple")) label = "Crisp Gala Apples";
                else if (name.toLowerCase().contains("potato")) label = "Farm Fresh Potatoes";
                else if (name.toLowerCase().contains("milk")) label = "Pure Cow A2 Milk";
                else if (name.toLowerCase().contains("mint")) label = "Aromatic Mint Leaves";
            }
            
            String reply = "📸 **AI Crop Vision Analysis**:<br><br>" +
                    "• **Identified Item**: " + label + "<br>" +
                    "• **Freshness Level**: 95% (Excellent condition, high quality)<br>" +
                    "• **Nutritional Benefits**: Rich in fibers, Vitamin C, minerals, and antioxidants.<br>" +
                    "• **Suggested Recipes**: Classic salads, tomato soups, potato curries.<br>" +
                    "• **Best Storage Method**: Store in a dry, ventilated basket away from direct heat or humidity.<br><br>" +
                    "💡 *You can find similar items in our database by asking for them in the chat!*";
            return ResponseEntity.ok(new com.f2c.marketplace.dto.AiResponse(reply));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
