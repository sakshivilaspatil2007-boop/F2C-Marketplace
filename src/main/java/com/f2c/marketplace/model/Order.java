package com.f2c.marketplace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "PLACED"; // PLACED, ACCEPTED, PROCESSING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // UPI, CARD, NET_BANKING, COD

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "PENDING"; // PENDING, COMPLETED, FAILED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }
}
