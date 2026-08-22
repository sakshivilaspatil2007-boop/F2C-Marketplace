package com.f2c.marketplace.repository;

import com.f2c.marketplace.model.ProductQualityScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductQualityScanRepository extends JpaRepository<ProductQualityScan, Long> {
    List<ProductQualityScan> findByProductIdOrderByScannedAtDesc(Long productId);

    @Query("SELECT s FROM ProductQualityScan s JOIN s.product p WHERE p.farmer.id = :farmerId ORDER BY s.scannedAt DESC")
    List<ProductQualityScan> findByFarmerId(@Param("farmerId") Long farmerId);
}
