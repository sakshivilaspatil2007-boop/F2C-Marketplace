package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.DeliveryTracking;
import com.f2c.marketplace.model.Order;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.service.OrderService;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam String shippingAddress,
                                    @RequestParam String paymentMethod,
                                    Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Order order = orderService.checkout(user.getId(), shippingAddress, paymentMethod);
            return ResponseEntity.ok(order);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerOrders(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<Order> orders = orderService.getCustomerOrders(user.getId());
            return ResponseEntity.ok(orders);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/farmer")
    public ResponseEntity<?> getFarmerOrders(Authentication authentication) {
        try {
            User farmer = userService.findByEmail(authentication.getName());
            List<Order> orders = orderService.getFarmerOrders(farmer.getId());
            return ResponseEntity.ok(orders);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId, Authentication authentication) {
        try {
            // Simply return the details, verify user role/id if required
            Order order = orderService.getOrderDetails(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/update-status/{orderId}")
    public ResponseEntity<?> updateStatus(@PathVariable Long orderId,
                                         @RequestParam String status,
                                         Authentication authentication) {
        try {
            Order updated = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/tracking/{orderId}")
    public ResponseEntity<?> getTracking(@PathVariable Long orderId) {
        try {
            DeliveryTracking tracking = orderService.getDeliveryTracking(orderId);
            return ResponseEntity.ok(tracking);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}



