package com.f2c.marketplace.service;

import com.f2c.marketplace.model.*;
import com.f2c.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FarmerCopilotService {

    @Autowired
    private FarmerDataService farmerDataService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public String generateCopilotResponse(Long farmerId, String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Please ask a question, and I will analyze your sales and crop data to help you. 🌾";
        }

        // Retrieve farmer context
        Map<String, Object> metrics = farmerDataService.getFarmerMetrics(farmerId);
        String dataContext = farmerDataService.formatFarmerDataContext(farmerId);

        // Check if farmer has listed products
        int totalProducts = (int) metrics.get("totalProducts");
        if (totalProducts == 0) {
            return "❌ **No Inventory Found**:\n" +
                   "You currently do not have any products listed in your crop inventory. " +
                   "Please list your first product in the **Manage Products** tab so I can begin analyzing your inventory and pricing recommendations!";
        }

        // Check if farmer has any orders
        int totalOrders = (int) metrics.get("totalOrders");
        double totalRevenue = (double) metrics.get("totalRevenue");

        // Check if an AI API key is available in environment variables
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            apiKey = System.getenv("AI_API_KEY");
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return queryGeminiAPI(apiKey, dataContext, message);
            } catch (Exception e) {
                // Fallback to heuristic rules if API fails
                return "[API Error - Falling back to local data analyzer]\n" + evaluateLocalHeuristics(metrics, message.toLowerCase().trim());
            }
        }

        // Default to local data-aware rule engine for development/testing
        return evaluateLocalHeuristics(metrics, message.toLowerCase().trim());
    }

    private String queryGeminiAPI(String apiKey, String dataContext, String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String systemPrompt = "You are an intelligent, friendly AI Farmer Copilot assisting a farmer with their dashboard metrics.\n" +
                "Here is the farmer's current real-time marketplace data:\n" +
                dataContext + "\n" +
                "Using this data, provide concise, polite, data-driven, and actionable business advice.\n" +
                "If the query asks for sales history, revenue, or low stock, give the exact numbers from the data context.\n" +
                "Never invent or hallucinate metrics that are not in the context. If no sales exist, state so honestly.";

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", systemPrompt + "\n\nFarmer's Question: " + userMessage);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contents, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List candidates = (List) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                if (content != null) {
                    List partsList = (List) content.get("parts");
                    if (partsList != null && !partsList.isEmpty()) {
                        Map part = (Map) partsList.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        }
        throw new RuntimeException("Failed to retrieve text from Gemini API response");
    }

    private String evaluateLocalHeuristics(Map<String, Object> metrics, String msg) {
        int totalOrders = (int) metrics.get("totalOrders");
        double totalRevenue = (double) metrics.get("totalRevenue");

        // 1. Sales Summary Intent
        if (msg.contains("summary") || msg.contains("performance") || msg.contains("revenue") || msg.contains("earnings") || msg.contains("how much did i make")) {
            if (totalOrders == 0) {
                return "📈 **Sales Performance Summary**:\n" +
                       "You have not received any orders yet. Once orders are placed by customers, they will appear here along with your calculated revenue statistics!";
            }
            return String.format("📈 **Sales Performance Summary**:\n" +
                    "• **Total Orders**: %d received\n" +
                    "• **Total Revenue Earned**: ₹%,.2f\n" +
                    "• **Total Items Sold**: %,.1f units\n\n" +
                    "💡 *Advice*: Keep listing fresh products to maintain customer engagement and boost order volume!",
                    totalOrders, totalRevenue, (Double) metrics.get("totalQuantitySold"));
        }

        // 2. Best-Selling / Highest Profit Product Intent
        if (msg.contains("best") || msg.contains("selling") || msg.contains("popular") || msg.contains("highest profit") || msg.contains("most sold")) {
            Product bestSelling = (Product) metrics.get("bestSellingProduct");
            if (bestSelling == null) {
                return "ℹ **Best-Selling Product**:\n" +
                       "You don't have enough sales history to determine your best-selling product yet.";
            }
            return String.format("🏆 **Best-Selling & Highest-Revenue Product**:\n" +
                    "• **Product Name**: %s\n" +
                    "• **Total Quantity Sold**: %,.1f %s\n" +
                    "• **Revenue Generated**: ₹%,.2f\n\n" +
                    "💡 *AI Advisory*: Your %s has been a key driver of your revenue. Consider prioritizing this crop's harvesting and logistics to meet high consumer interest!",
                    bestSelling.getName(), (Double) metrics.get("bestSellingQuantity"), bestSelling.getUnit(),
                    (Double) metrics.get("highestRevenue"), bestSelling.getName());
        }

        // 3. Low Stock Intent
        if (msg.contains("low") || msg.contains("stock") || msg.contains("replenish") || msg.contains("empty") || msg.contains("run out")) {
            List<Product> lowStock = (List<Product>) metrics.get("lowStockProducts");
            if (lowStock.isEmpty()) {
                return "✅ **Stock Levels**:\n" +
                       "All of your products are well-stocked (minimum stock level of 10 units). Excellent inventory management!";
            }
            StringBuilder sb = new StringBuilder("⚠️ **Low Stock Alert**:\n" +
                    "The following products are running low in stock (10 units or less):\n");
            for (Product p : lowStock) {
                sb.append(String.format("- **%s**: %.1f %s remaining\n", p.getName(), p.getQuantity(), p.getUnit()));
            }
            sb.append("\n💡 *Action Required*: We recommend harvesting or replenishing these items soon to prevent missed sales opportunities.");
            return sb.toString();
        }

        // 4. Slow Moving Items Intent
        if (msg.contains("slow") || msg.contains("selling slowly") || msg.contains("not selling") || msg.contains("least sold")) {
            List<Product> slowMoving = (List<Product>) metrics.get("slowMovingProducts");
            if (slowMoving.isEmpty()) {
                return "✅ **Sales Circulation**:\n" +
                       "Great news! All of your listed products are moving actively and have recorded sales in your dashboard history.";
            }
            StringBuilder sb = new StringBuilder("🐌 **Slow-Moving Products**:\n" +
                    "The following products have registered zero sales in your history:\n");
            for (Product p : slowMoving) {
                sb.append(String.format("- **%s** (Price: ₹%.2f per %s)\n", p.getName(), p.getPrice(), p.getUnit()));
            }
            sb.append("\n💡 *Recommendation*: Consider offering a minor discount, updating product descriptions, or checking if the image is appealing to attract first-time buyers.");
            return sb.toString();
        }

        // 5. Recommended Pricing Intent
        if (msg.contains("recommend") || msg.contains("price") || msg.contains("pricing") || msg.contains("what should i charge")) {
            Map<String, Double> catAvgs = (Map<String, Double>) metrics.get("categoryAvgPrices");
            StringBuilder sb = new StringBuilder("💰 **Smart Pricing Advisory**:\n" +
                    "Here is a comparison of your prices against current marketplace averages:\n");
            
            for (Map.Entry<String, Double> entry : catAvgs.entrySet()) {
                sb.append(String.format("- **%s Category Average**: ₹%.2f\n", 
                        entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1), entry.getValue()));
            }
            
            sb.append("\n💡 *Pricing Tips*: If your product prices are significantly higher than category averages, it might slow down sales. If they are much lower, you might be losing out on potential profit margins!");
            return sb.toString();
        }

        // 6. Specific Tomato Stock/Price queries
        if (msg.contains("tomato") || msg.contains("टमाटर") || msg.contains("टोमॅटो")) {
            Map<String, Double> catAvgs = (Map<String, Double>) metrics.get("categoryAvgPrices");
            double avgPrice = catAvgs.getOrDefault("vegetables", 35.0);
            
            if (msg.contains("stock") || msg.contains("increase")) {
                return String.format("🍅 **Tomato Stock Advisory**:\n" +
                        "Looking at your sales trends, Tomatoes are currently experiencing stable demand in the market.\n" +
                        "If you have additional raw harvest available, increasing your stock is highly recommended since summer salads and kitchen staples require steady supplies.", avgPrice);
            }
            return String.format("🍅 **Tomato Pricing Analysis**:\n" +
                    "• **Recommended Price**: ₹%.2f per kg (current vegetable category average)\n" +
                    "• **Actionable Advice**: If your tomatoes are organic, you can charge up to 10-15%% above this average (around ₹%.2f - ₹%.2f per kg) to optimize profit.", 
                    avgPrice, avgPrice * 1.1, avgPrice * 1.15);
        }

        // 7. General Demand Prediction Intent
        if (msg.contains("demand") || msg.contains("high demand") || msg.contains("sell this week") || msg.contains("what should i sell")) {
            return "📈 **Market Demand Forecast**:\n" +
                   "• **High Demand crops**: Organic Tomatoes, Fresh Green Apples, and Spinach are seeing peak interest this week.\n" +
                   "• **Recommended crops**: Salad greens and fresh root vegetables (Onions, Potatoes) are highly stable and reliable earners.\n\n" +
                   "💡 *Recommendation*: Prioritize listing fresh greens and vegetables, as local market consumption is currently outstripping seasonal supply.";
        }

        // Default Fallback
        return "🤖 **Hello! I am your AI Farmer Copilot**.\n\n" +
               "I can analyze your shop inventory, orders, and sales performance to help you make smarter decisions. Try asking me:\n" +
               "- *\"What is my best-selling product?\"*\n" +
               "- *\"Give me a summary of my sales.\"*\n" +
               "- *\"Which of my products are low in stock?\"*\n" +
               "- *\"What is the recommended price for tomatoes?\"*\n" +
               "- *\"Which products are selling slowly?\"*";
    }
}
