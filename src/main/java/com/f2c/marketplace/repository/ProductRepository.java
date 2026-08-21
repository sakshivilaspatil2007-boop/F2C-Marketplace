package com.f2c.marketplace.repository;

import com.f2c.marketplace.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFarmerId(Long farmerId);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatus(String status);
    List<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status);
}
