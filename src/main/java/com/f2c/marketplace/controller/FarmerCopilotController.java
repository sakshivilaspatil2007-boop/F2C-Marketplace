package com.f2c.marketplace.controller;

import com.f2c.marketplace.dto.FarmerCopilotRequest;
import com.f2c.marketplace.dto.FarmerCopilotResponse;
import com.f2c.marketplace.model.FarmerCopilotChat;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.FarmerCopilotChatRepository;
import com.f2c.marketplace.service.FarmerCopilotService;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/farmer/copilot")
public class FarmerCopilotController {

    @Autowired
    private FarmerCopilotService farmerCopilotService;

    @Autowired
    private UserService userService;

    @Autowired
    private FarmerCopilotChatRepository farmerCopilotChatRepository;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody FarmerCopilotRequest request, Authentication authentication) {
        try {
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message cannot be empty");
            }

            User farmer = userService.findByEmail(authentication.getName());
            String responseText = farmerCopilotService.generateCopilotResponse(farmer.getId(), request.getMessage());

            // Save chat history to database
            FarmerCopilotChat chat = new FarmerCopilotChat();
            chat.setFarmer(farmer);
            chat.setUserMessage(request.getMessage());
            chat.setAiResponse(responseText);
            farmerCopilotChatRepository.save(chat);

            return ResponseEntity.ok(new FarmerCopilotResponse(responseText));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Sorry, I couldn't process your request right now. Please try again.");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            User farmer = userService.findByEmail(authentication.getName());
            List<FarmerCopilotChat> history = farmerCopilotChatRepository.findByFarmerIdOrderByCreatedAtAsc(farmer.getId());
            return ResponseEntity.ok(history);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to retrieve chat history");
        }
    }

    @PostMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearHistory(Authentication authentication) {
        try {
            User farmer = userService.findByEmail(authentication.getName());
            farmerCopilotChatRepository.deleteByFarmerId(farmer.getId());
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to clear chat history");
        }
    }
}
