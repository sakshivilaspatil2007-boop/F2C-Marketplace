package com.f2c.marketplace.repository;

import com.f2c.marketplace.model.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {
    Optional<DeliveryTracking> findByOrderId(Long orderId);
}
