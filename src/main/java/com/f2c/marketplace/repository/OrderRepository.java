package com.f2c.marketplace.repository;

import com.f2c.marketplace.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findDistinctByItemsProductFarmerIdOrderByOrderDateDesc(Long farmerId);
}
