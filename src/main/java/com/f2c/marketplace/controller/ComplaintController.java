package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.Complaint;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.ComplaintRepository;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addComplaint(@RequestParam String subject,
                                           @RequestParam String message,
                                           Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Complaint complaint = new Complaint();
            complaint.setUser(user);
            complaint.setSubject(subject);
            complaint.setMessage(message);
            complaint.setStatus("PENDING");

            Complaint saved = complaintRepository.save(complaint);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
