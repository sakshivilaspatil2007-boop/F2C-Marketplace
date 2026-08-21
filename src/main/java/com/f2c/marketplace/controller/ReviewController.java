package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.Review;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.ProductRepository;
import com.f2c.marketplace.repository.ReviewRepository;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/product/{productId}")
public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
    System.out.println("GET Reviews API called for product: " + productId);
    return ResponseEntity.ok(reviewRepository.findByProductId(productId));
}
    

    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestParam Long productId,
                                        @RequestParam Integer rating,
                                        @RequestParam String comment,
                                        Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
            }

            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);

            Review savedReview = reviewRepository.save(review);
            return ResponseEntity.ok(savedReview);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
