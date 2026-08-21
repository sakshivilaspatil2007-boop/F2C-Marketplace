package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.*;
import com.f2c.marketplace.repository.*;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            List<User> users = userRepository.findAll();
            List<Order> orders = orderRepository.findAll();
            List<Product> products = productRepository.findAll();
            
            long totalCustomers = users.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
            long totalFarmers = users.stream().filter(u -> u.getRole() == Role.FARMER).count();
            
            double totalSales = orders.stream()
                    .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                    .mapToDouble(Order::getTotalAmount)
                    .sum();

            metrics.put("totalUsers", users.size());
            metrics.put("totalCustomers", totalCustomers);
            metrics.put("totalFarmers", totalFarmers);
            metrics.put("totalOrders", orders.size());
            metrics.put("totalProducts", products.size());
            metrics.put("totalSales", totalSales);

            return ResponseEntity.ok(metrics);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/farmers/all")
    public ResponseEntity<List<Farmer>> getAllFarmers() {
        return ResponseEntity.ok(userService.getAllFarmers());
    }

    @PutMapping("/farmers/approve/{farmerId}")
    public ResponseEntity<?> approveFarmer(@PathVariable Long farmerId) {
        try {
            userService.approveFarmer(farmerId);
            return ResponseEntity.ok("Farmer profile approved successfully");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/farmers/reject/{farmerId}")
    public ResponseEntity<?> rejectFarmer(@PathVariable Long farmerId) {
        try {
            userService.rejectFarmer(farmerId);
            return ResponseEntity.ok("Farmer profile rejected successfully");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/suspend/{userId}")
    public ResponseEntity<?> suspendUser(@PathVariable Long userId) {
        try {
            userService.updateUserStatus(userId, "SUSPENDED");
            return ResponseEntity.ok("User suspended successfully");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/users/activate/{userId}")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            userService.updateUserStatus(userId, "ACTIVE");
            return ResponseEntity.ok("User activated successfully");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintRepository.findAll());
    }

    @PutMapping("/complaints/resolve/{complaintId}")
    public ResponseEntity<?> resolveComplaint(@PathVariable Long complaintId) {
        try {
            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));
            complaint.setStatus("RESOLVED");
            complaintRepository.save(complaint);
            return ResponseEntity.ok("Complaint marked as resolved");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
