package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.WishlistItem;
import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.WishlistItemRepository;
import com.f2c.marketplace.repository.ProductRepository;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getWishlist(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<WishlistItem> items = wishlistItemRepository.findByUserId(user.getId());
            return ResponseEntity.ok(items);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToWishlist(@RequestParam Long productId,
                                           Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Optional<WishlistItem> existingItemOpt = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId);
            if (existingItemOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Product is already in wishlist");
            }

            WishlistItem wishlistItem = new WishlistItem();
            wishlistItem.setUser(user);
            wishlistItem.setProduct(product);

            WishlistItem savedItem = wishlistItemRepository.save(wishlistItem);
            return ResponseEntity.ok(savedItem);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId,
                                                Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                    .orElseThrow(() -> new RuntimeException("Product not found in wishlist"));

            wishlistItemRepository.delete(item);
            return ResponseEntity.ok("Item removed from wishlist");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
