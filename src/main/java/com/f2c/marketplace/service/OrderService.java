package com.f2c.marketplace.service;

import com.f2c.marketplace.model.*;
import com.f2c.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private DeliveryTrackingRepository deliveryTrackingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Order checkout(Long userId, String shippingAddress, String paymentMethod) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PLACED");
        order.setPaymentStatus("COD".equalsIgnoreCase(paymentMethod) ? "PENDING" : "COMPLETED");

        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Deduct stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            totalAmount += product.getPrice() * cartItem.getQuantity();
            orderItems.add(orderItem);

            // Notify Farmer of new order item
            Notification farmerNotif = new Notification();
            farmerNotif.setUser(product.getFarmer());
            farmerNotif.setMessage("New order placed for " + cartItem.getQuantity() + " " + product.getUnit() + " of " + product.getName() + " by " + user.getName());
            notificationRepository.save(farmerNotif);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Create Delivery Tracking
        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setOrder(savedOrder);
        tracking.setStatus("PLACED");
        tracking.setLatitude(28.6139); // Initial coordinates (e.g. New Delhi)
        tracking.setLongitude(77.2090);
        tracking.setEstimatedDeliveryTime("45 mins");
        deliveryTrackingRepository.save(tracking);

        // Empty Cart
        cartItemRepository.deleteByUserId(userId);

        // Notify Customer
        Notification customerNotif = new Notification();
        customerNotif.setUser(user);
        customerNotif.setMessage("Your order #" + savedOrder.getId() + " has been placed successfully!");
        notificationRepository.save(customerNotif);

        return savedOrder;
    }

    public List<Order> getCustomerOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public List<Order> getFarmerOrders(Long farmerId) {
        return orderRepository.findDistinctByItemsProductFarmerIdOrderByOrderDateDesc(farmerId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order getOrderDetails(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderDetails(orderId);
        order.setStatus(status);

        // Update tracking status
        Optional<DeliveryTracking> trackingOpt = deliveryTrackingRepository.findByOrderId(orderId);
        if (trackingOpt.isPresent()) {
            DeliveryTracking tracking = trackingOpt.get();
            tracking.setStatus(status);
            
            // Mock delivery driver coordinates movement
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                tracking.setLatitude(28.6150);
                tracking.setLongitude(77.2110);
                tracking.setEstimatedDeliveryTime("35 mins");
            } else if ("PROCESSING".equalsIgnoreCase(status)) {
                tracking.setLatitude(28.6180);
                tracking.setLongitude(77.2150);
                tracking.setEstimatedDeliveryTime("25 mins");
            } else if ("SHIPPED".equalsIgnoreCase(status)) {
                tracking.setLatitude(28.6220);
                tracking.setLongitude(77.2200);
                tracking.setEstimatedDeliveryTime("15 mins");
            } else if ("OUT_FOR_DELIVERY".equalsIgnoreCase(status)) {
                tracking.setLatitude(28.6280);
                tracking.setLongitude(77.2280);
                tracking.setEstimatedDeliveryTime("5 mins");
            } else if ("DELIVERED".equalsIgnoreCase(status)) {
                tracking.setLatitude(28.6304);
                tracking.setLongitude(77.2345);
                tracking.setEstimatedDeliveryTime("Delivered");
                order.setPaymentStatus("COMPLETED");
            }
            deliveryTrackingRepository.save(tracking);
        }

        // If delivered, credit farmer revenue
        if ("DELIVERED".equalsIgnoreCase(status)) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                User farmerUser = product.getFarmer();
                Optional<Farmer> farmerOpt = farmerRepository.findByUserId(farmerUser.getId());
                if (farmerOpt.isPresent()) {
                    Farmer farmer = farmerOpt.get();
                    double itemRevenue = item.getPrice() * item.getQuantity();
                    farmer.setRevenue(farmer.getRevenue() + itemRevenue);
                    farmerRepository.save(farmer);
                }
            }
        }

        // Notify customer
        Notification customerNotif = new Notification();
        customerNotif.setUser(order.getUser());
        customerNotif.setMessage("Your order #" + order.getId() + " is now: " + status);
        notificationRepository.save(customerNotif);

        return orderRepository.save(order);
    }

    public DeliveryTracking getDeliveryTracking(Long orderId) {
        return deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Tracking details not found"));
    }
}
