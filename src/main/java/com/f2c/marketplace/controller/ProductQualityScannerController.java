package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.ProductQualityScan;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.ProductQualityScanRepository;
import com.f2c.marketplace.repository.ProductRepository;
import com.f2c.marketplace.service.ProductQualityScannerService;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quality-scanner")
public class ProductQualityScannerController {

    @Autowired
    private ProductQualityScannerService qualityScannerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductQualityScanRepository qualityScanRepository;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            Map<String, Object> analysisResult = qualityScannerService.analyzeProductQuality(file);
            return ResponseEntity.ok(analysisResult);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Unable to analyze the image right now. Please try again later.");
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getScanByProduct(@PathVariable Long productId, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Check if user is the farmer who owns the product, or is admin
            if (!user.getRole().toString().equalsIgnoreCase("ADMIN") && !product.getFarmer().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized access to scan history");
            }

            List<ProductQualityScan> scans = qualityScanRepository.findByProductIdOrderByScannedAtDesc(productId);
            return ResponseEntity.ok(scans);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            User farmer = userService.findByEmail(authentication.getName());
            if (!farmer.getRole().toString().equalsIgnoreCase("FARMER")) {
                return ResponseEntity.status(403).body("Only farmers can view scan history");
            }
            List<ProductQualityScan> scans = qualityScanRepository.findByFarmerId(farmer.getId());
            return ResponseEntity.ok(scans);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
