package com.f2c.marketplace.service;

import com.f2c.marketplace.model.Category;
import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.ProductQualityScan;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.CategoryRepository;
import com.f2c.marketplace.repository.ProductRepository;
import com.f2c.marketplace.repository.UserRepository;
import com.f2c.marketplace.repository.ProductQualityScanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductQualityScanRepository qualityScanRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatus("ACTIVE");
    }

    public List<Product> getFarmerProducts(Long farmerId) {
        return productRepository.findByFarmerId(farmerId);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseAndStatus(query, "ACTIVE");
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product getProductDetails(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product addProduct(Product product, Long farmerId, Long categoryId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer user not found"));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setFarmer(farmer);
        product.setCategory(category);
        product.setStatus("ACTIVE");

        Product savedProduct = productRepository.save(product);

        // If quality check score is present, record it in scan logs
        if (savedProduct.getQualityScore() != null) {
            saveScanLog(savedProduct);
        }

        return savedProduct;
    }

    public Product updateProduct(Long productId, Product updatedProduct) {
        Product existingProduct = getProductDetails(productId);
        
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setUnit(updatedProduct.getUnit());
        existingProduct.setQualityScore(updatedProduct.getQualityScore());
        existingProduct.setQualityGrade(updatedProduct.getQualityGrade());
        
        if (updatedProduct.getImageUrl() != null) {
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
        }
        if (updatedProduct.getStatus() != null) {
            existingProduct.setStatus(updatedProduct.getStatus());
        }

        Product savedProduct = productRepository.save(existingProduct);

        // If quality check score is present, record it in scan logs
        if (savedProduct.getQualityScore() != null) {
            saveScanLog(savedProduct);
        }

        return savedProduct;
    }

    private void saveScanLog(Product product) {
        try {
            ProductQualityScan scan = new ProductQualityScan();
            scan.setProduct(product);
            scan.setImageUrl(product.getImageUrl());
            scan.setProductType(product.getCategory() != null ? product.getCategory().getName() : "Produce");
            scan.setQualityScore(product.getQualityScore());
            
            String grade = product.getQualityGrade() != null ? product.getQualityGrade() : "GOOD";
            scan.setQualityGrade(grade);
            
            int score = product.getQualityScore();
            if (score >= 90) {
                scan.setFreshness("High");
                scan.setVisibleDefects("None visible");
                scan.setRecommendation("Excellent quality. Suitable for premium sale.");
            } else if (score >= 80) {
                scan.setFreshness("High");
                scan.setVisibleDefects("Minor surface spots detected");
                scan.setRecommendation("Good quality. Suitable for normal marketplace sale.");
            } else {
                scan.setFreshness("Medium");
                scan.setVisibleDefects("Minor bruising");
                scan.setRecommendation("Average quality. Consider sorting before selling.");
            }
            
            qualityScanRepository.save(scan);
        } catch (Exception e) {
            // Log warning but prevent blocking the main product transaction
            System.err.println("Warning: failed to write quality scan log record: " + e.getMessage());
        }
    }

    public void deleteProduct(Long productId) {
        Product product = getProductDetails(productId);
        productRepository.delete(product);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }
}
