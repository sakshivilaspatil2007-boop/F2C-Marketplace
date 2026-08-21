package com.f2c.marketplace.service;

import com.f2c.marketplace.model.Category;
import com.f2c.marketplace.model.Product;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.CategoryRepository;
import com.f2c.marketplace.repository.ProductRepository;
import com.f2c.marketplace.repository.UserRepository;
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

        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, Product updatedProduct) {
        Product existingProduct = getProductDetails(productId);
        
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setUnit(updatedProduct.getUnit());
        if (updatedProduct.getImageUrl() != null) {
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
        }
        if (updatedProduct.getStatus() != null) {
            existingProduct.setStatus(updatedProduct.getStatus());
        }

        return productRepository.save(existingProduct);
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
