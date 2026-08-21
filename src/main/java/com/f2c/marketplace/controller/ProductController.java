package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.Category;
import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.FarmerRepository;
import com.f2c.marketplace.service.ProductService;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private FarmerRepository farmerRepository;

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/api/products/public/list")
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/api/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    @GetMapping("/api/products/category/{categoryId}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/api/products/details/{productId}")
    public ResponseEntity<Product> getDetails(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(productService.getProductDetails(productId));
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/categories/public/list")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    // --- FARMER ENDPOINTS (CRUD) ---

    @GetMapping("/api/farmer/products")
    public ResponseEntity<?> getFarmerProducts(Authentication authentication) {
        try {
            User farmer = userService.findByEmail(authentication.getName());
            return ResponseEntity.ok(productService.getFarmerProducts(farmer.getId()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/api/farmer/profile")
    public ResponseEntity<?> getFarmerProfile(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            return farmerRepository.findByUserId(user.getId())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/api/farmer/products/add")
    public ResponseEntity<?> addProduct(@RequestBody Product product, 
                                        @RequestParam Long categoryId,
                                        Authentication authentication) {
        try {
            User farmer = userService.findByEmail(authentication.getName());
            Product savedProduct = productService.addProduct(product, farmer.getId(), categoryId);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/api/farmer/products/update/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                        @RequestBody Product product,
                                        Authentication authentication) {
        try {
            // Verify ownership
            User farmer = userService.findByEmail(authentication.getName());
            Product existing = productService.getProductDetails(productId);
            if (!existing.getFarmer().getId().equals(farmer.getId())) {
                return ResponseEntity.status(403).body("You are not authorized to update this product");
            }
            Product updated = productService.updateProduct(productId, product);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/api/farmer/products/delete/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId,
                                           Authentication authentication) {
        try {
            // Verify ownership
            User farmer = userService.findByEmail(authentication.getName());
            Product existing = productService.getProductDetails(productId);
            if (!existing.getFarmer().getId().equals(farmer.getId())) {
                return ResponseEntity.status(403).body("You are not authorized to delete this product");
            }
            productService.deleteProduct(productId);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
