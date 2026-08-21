package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.CartItem;
import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.CartItemRepository;
import com.f2c.marketplace.repository.ProductRepository;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getCart(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<CartItem> items = cartItemRepository.findByUserId(user.getId());
            return ResponseEntity.ok(items);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long productId, 
                                       @RequestParam Double quantity,
                                       Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < quantity) {
                return ResponseEntity.badRequest().body("Insufficient stock available");
            }

            Optional<CartItem> existingItemOpt = cartItemRepository.findByUserIdAndProductId(user.getId(), productId);
            CartItem cartItem;
            if (existingItemOpt.isPresent()) {
                cartItem = existingItemOpt.get();
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
            } else {
                cartItem = new CartItem();
                cartItem.setUser(user);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
            }

            CartItem savedItem = cartItemRepository.save(cartItem);
            return ResponseEntity.ok(savedItem);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<?> updateCartQuantity(@PathVariable Long itemId,
                                                @RequestParam Double quantity,
                                                Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            if (!item.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized access");
            }

            if (item.getProduct().getQuantity() < quantity) {
                return ResponseEntity.badRequest().body("Insufficient stock available");
            }

            item.setQuantity(quantity);
            CartItem updated = cartItemRepository.save(item);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long itemId,
                                            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            if (!item.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized access");
            }

            cartItemRepository.delete(item);
            return ResponseEntity.ok("Item removed from cart");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            cartItemRepository.deleteByUserId(user.getId());
            return ResponseEntity.ok("Cart cleared");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
