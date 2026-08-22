package com.f2c.marketplace.service;

import com.f2c.marketplace.model.*;
import com.f2c.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FarmerDataService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public Map<String, Object> getFarmerMetrics(Long farmerId) {
        Map<String, Object> metrics = new HashMap<>();

        // 1. Retrieve products
        List<Product> products = productRepository.findByFarmerId(farmerId);
        metrics.put("totalProducts", products.size());

        // 2. Retrieve orders
        List<Order> orders = orderRepository.findDistinctByItemsProductFarmerIdOrderByOrderDateDesc(farmerId);
        metrics.put("totalOrders", orders.size());

        // 3. Aggregate sales statistics
        double totalRevenue = 0.0;
        double totalQuantitySold = 0.0;
        Map<Long, Double> productQuantities = new HashMap<>();
        Map<Long, Double> productRevenue = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();

        for (Product p : products) {
            productMap.put(p.getId(), p);
            productQuantities.put(p.getId(), 0.0);
            productRevenue.put(p.getId(), 0.0);
        }

        for (Order order : orders) {
            // Only aggregate completed or active orders (exclude cancelled if appropriate, or include all for historical)
            if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getFarmer().getId().equals(farmerId)) {
                    double qty = item.getQuantity();
                    double price = item.getPrice();
                    double revenue = qty * price;

                    totalRevenue += revenue;
                    totalQuantitySold += qty;

                    productQuantities.put(product.getId(), productQuantities.getOrDefault(product.getId(), 0.0) + qty);
                    productRevenue.put(product.getId(), productRevenue.getOrDefault(product.getId(), 0.0) + revenue);
                }
            }
        }

        metrics.put("totalRevenue", totalRevenue);
        metrics.put("totalQuantitySold", totalQuantitySold);

        // 4. Find best-selling and highest-revenue products
        Product bestSellingProduct = null;
        double maxQty = 0.0;
        Product highestRevenueProduct = null;
        double maxRev = 0.0;

        List<Product> lowStockProducts = new ArrayList<>();
        List<Product> slowMovingProducts = new ArrayList<>();

        for (Product p : products) {
            double qtySold = productQuantities.getOrDefault(p.getId(), 0.0);
            double rev = productRevenue.getOrDefault(p.getId(), 0.0);

            if (qtySold > maxQty) {
                maxQty = qtySold;
                bestSellingProduct = p;
            }
            if (rev > maxRev) {
                maxRev = rev;
                highestRevenueProduct = p;
            }

            // Low stock checks (quantity <= 10)
            if (p.getQuantity() <= 10.0) {
                lowStockProducts.add(p);
            }

            // Slow moving checks (listed but zero sales)
            if (qtySold == 0.0) {
                slowMovingProducts.add(p);
            }
        }

        metrics.put("bestSellingProduct", bestSellingProduct);
        metrics.put("bestSellingQuantity", maxQty);
        metrics.put("highestRevenueProduct", highestRevenueProduct);
        metrics.put("highestRevenue", maxRev);
        metrics.put("lowStockProducts", lowStockProducts);
        metrics.put("slowMovingProducts", slowMovingProducts);

        // 5. Category average prices (market pricing)
        List<Category> categories = categoryRepository.findAll();
        Map<String, Double> categoryAvgPrices = new HashMap<>();
        List<Product> allActiveProducts = productRepository.findByStatus("ACTIVE");

        for (Category cat : categories) {
            double sum = 0.0;
            int count = 0;
            for (Product p : allActiveProducts) {
                if (p.getCategory().getId().equals(cat.getId())) {
                    sum += p.getPrice();
                    count++;
                }
            }
            double avg = count > 0 ? (sum / count) : 0.0;
            categoryAvgPrices.put(cat.getName().toLowerCase(), avg);
        }
        metrics.put("categoryAvgPrices", categoryAvgPrices);

        return metrics;
    }

    public String formatFarmerDataContext(Long farmerId) {
        Map<String, Object> metrics = getFarmerMetrics(farmerId);
        StringBuilder sb = new StringBuilder();

        sb.append("Farmer ID: ").append(farmerId).append("\n");
        sb.append("Total Revenue: ₹").append(String.format("%.2f", (Double) metrics.get("totalRevenue"))).append("\n");
        sb.append("Total Orders Received: ").append(metrics.get("totalOrders")).append("\n");
        sb.append("Total Quantity Sold: ").append(metrics.get("totalQuantitySold")).append(" units\n");

        Product bestSelling = (Product) metrics.get("bestSellingProduct");
        if (bestSelling != null) {
            sb.append("Best Selling Product: ").append(bestSelling.getName())
              .append(" (Sold: ").append(metrics.get("bestSellingQuantity")).append(" ").append(bestSelling.getUnit()).append(")\n");
        } else {
            sb.append("Best Selling Product: None (No sales yet)\n");
        }

        Product highestRev = (Product) metrics.get("highestRevenueProduct");
        if (highestRev != null) {
            sb.append("Highest Revenue Product: ").append(highestRev.getName())
              .append(" (Generated: ₹").append(String.format("%.2f", (Double) metrics.get("highestRevenue"))).append(")\n");
        } else {
            sb.append("Highest Revenue Product: None\n");
        }

        List<Product> lowStock = (List<Product>) metrics.get("lowStockProducts");
        if (!lowStock.isEmpty()) {
            sb.append("Low Stock Products: ");
            for (Product p : lowStock) {
                sb.append(p.getName()).append(" (Stock: ").append(p.getQuantity()).append(" ").append(p.getUnit()).append("), ");
            }
            sb.append("\n");
        } else {
            sb.append("Low Stock Products: None\n");
        }

        List<Product> slowMoving = (List<Product>) metrics.get("slowMovingProducts");
        if (!slowMoving.isEmpty()) {
            sb.append("Slow Moving Products: ");
            for (Product p : slowMoving) {
                sb.append(p.getName()).append(", ");
            }
            sb.append("\n");
        } else {
            sb.append("Slow Moving Products: None\n");
        }

        // List farmer's entire inventory
        List<Product> products = productRepository.findByFarmerId(farmerId);
        sb.append("Inventory Details:\n");
        for (Product p : products) {
            sb.append("- ").append(p.getName()).append(": Price ₹").append(p.getPrice()).append(" per ").append(p.getUnit())
              .append(", Stock: ").append(p.getQuantity()).append(" ").append(p.getUnit()).append("\n");
        }

        // List category average prices
        Map<String, Double> catAvgs = (Map<String, Double>) metrics.get("categoryAvgPrices");
        sb.append("Market Reference Average Prices:\n");
        for (Map.Entry<String, Double> entry : catAvgs.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ₹").append(String.format("%.2f", entry.getValue())).append("\n");
        }

        return sb.toString();
    }
}
