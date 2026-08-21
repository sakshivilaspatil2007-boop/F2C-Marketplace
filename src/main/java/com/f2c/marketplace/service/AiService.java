package com.f2c.marketplace.service;

import com.f2c.marketplace.model.*;
import com.f2c.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // AI Memory storage (Feature 19)
    private static class UserMemory {
        List<String> searches = new ArrayList<>();
        Set<String> favoriteIngredients = new LinkedHashSet<>();
        String detectedLanguage = "english";
    }

    private final Map<Long, UserMemory> memories = new ConcurrentHashMap<>();

    private UserMemory getUserMemory(Long userId) {
        if (userId == null) return new UserMemory();
        return memories.computeIfAbsent(userId, k -> new UserMemory());
    }

    public String chat(String message, Long userId) {
        if (message == null || message.trim().isEmpty()) {
            return "Please tell me how I can help you today! 😊";
        }

        String msg = message.toLowerCase().trim();
        String lang = detectLanguage(msg);

        // Record User Memory (Feature 19)
        UserMemory memory = getUserMemory(userId);
        memory.detectedLanguage = lang;
        memory.searches.add(msg);
        
        List<Product> allProductsForMemory = productRepository.findByStatus("ACTIVE");
        for (Product p : allProductsForMemory) {
            if (msg.contains(p.getName().toLowerCase())) {
                memory.favoriteIngredients.add(p.getName());
            }
        }

        // 1. Check for Greetings
        if (msg.matches(".*\\b(hello|hi|hey|hola|greetings|नमस्कार|नमस्ते|हॅलो)\\b.*")) {
            return getGreeting(lang);
        }

        // 2. Shopping Assistant / Bill Estimator (Feature 13)
        if (msg.contains("bill") || msg.contains("estimate") || msg.contains("calculate") || msg.contains("cost") || msg.contains("price of") || msg.contains("किंमत") || msg.contains("हिशोब")) {
            return estimateBill(msg, lang);
        }

        // 3. Daily Health Tips & Hydration (Feature 14)
        if (msg.contains("tip") || msg.contains("health tip") || msg.contains("hydration") || msg.contains("remind") || msg.contains("पाणी") || msg.contains("आरोग्य सल्ला")) {
            return getDailyHealthTips(lang);
        }

        // 4. Order Assistance
        if (msg.contains("order") || msg.contains("track") || msg.contains("status") || msg.contains("डिलिव्हरी") || msg.contains("ऑर्डर")) {
            return getOrderAssistance(userId, msg, lang);
        }

        // 3. Farming Knowledge (Feature 7)
        if (msg.contains("grow") || msg.contains("pest") || msg.contains("irrigation") || msg.contains("compost") || 
            msg.contains("farming") || msg.contains("fertilizer") || msg.contains("शेतकरी") || msg.contains("पिक") || msg.contains("खत")) {
            return getFarmingKnowledge(msg, lang);
        }

        // 4. Recipes & Cooking Assistant (Feature 1, 8, 9)
        if (msg.contains("recipe") || msg.contains("how to make") || msg.contains("how to cook") || msg.contains("dishes") ||
            msg.contains("रेसिपी") || msg.contains("कृती") || msg.contains("कсе बनवायचे") || msg.contains("बनाएं") ||
            msg.contains("breakfast recipe") || msg.contains("dinner recipe") || msg.contains("diwali recipe") || msg.contains("festival sweet") ||
            (msg.contains("cook") && !msg.contains("how to grow") && !msg.contains("farming"))) {
            return handleRecipeQueries(msg, lang);
        }

        // 5. Seasonal Produce (Feature 2)
        if (msg.contains("season") || msg.contains("month") || msg.contains("july") || msg.contains("summer") || msg.contains("winter") || 
            msg.contains("monsoon") || msg.contains("हंगामी") || msg.contains("फल") || msg.contains("सब्जी")) {
            return getSeasonalProduce(msg, lang);
        }

        // 6. Healthy Advisor & Nutrition Expert (Feature 3, 6, 14)
        if (msg.contains("healthy") || msg.contains("diet") || msg.contains("nutrition") || msg.contains("protein") || 
            msg.contains("calorie") || msg.contains("diabetes") || msg.contains("pressure") || msg.contains("weight") || 
            msg.contains("skin") || msg.contains("hair") || msg.contains("पोषक") || msg.contains("आरोग्य")) {
            return handleNutritionAndHealth(msg, lang);
        }

        // 7. Shopping Assistant & Product queries (Feature 4, 5, 13)
        return handleProductAndSearch(msg, lang);
    }

    // --- LANGUAGE DETECTION ---
    private String detectLanguage(String msg) {
        if (msg.matches(".*[अ-ज्ञ].*")) {
            // Marathi detector
            if (msg.contains("कृती") || msg.contains("माहिती") || msg.contains("शेतकरी") || msg.contains("माझी") || msg.contains("सांगा")) {
                return "marathi";
            }
            return "hindi";
        }
        return "english";
    }

    // --- GREETINGS (Feature 18) ---
    private String getGreeting(String lang) {
        int hour = LocalDateTime.now().getHour();
        String timeGreeting;
        if (hour < 12) {
            timeGreeting = lang.equals("marathi") ? "शुभ सकाळ! 🌞" : (lang.equals("hindi") ? "सुप्रभात! 🌞" : "Good Morning! 🌞 Ready to eat healthy today?");
        } else if (hour < 17) {
            timeGreeting = lang.equals("marathi") ? "शुभ दुपार! 🌾" : (lang.equals("hindi") ? "नमस्कार! 🌾" : "Good Afternoon! Looking for fresh farm products?");
        } else {
            timeGreeting = lang.equals("marathi") ? "शुभ संध्याकाळ! 🌌" : (lang.equals("hindi") ? "शुभ संध्या! 🌌" : "Good Evening! Need dinner recipe ideas?");
        }

        if (lang.equals("marathi")) {
            return timeGreeting + " मी आपला F2C स्मार्ट असिस्टंट आहे. मी आपल्याला रेसिपी, हंगामी पिके, पौष्टिक माहिती किंवा ऑर्डर ट्रॅक करण्यात मदत करू शकतो. विचारण्यासाठी खालील बटणावर क्लिक करा!";
        } else if (lang.equals("hindi")) {
            return timeGreeting + " मैं आपका F2C स्मार्ट असिस्टंट हूँ। मैं आपको रेसिपी, मौसमी फल-सब्जियां, पोषण जानकारी और ऑर्डर ट्रैक करने में मदद कर सकता हूँ। पूछने के लिए नीचे दिए गए बटनों का उपयोग करें!";
        }
        return timeGreeting + " I am your F2C Smart AI Assistant. 🌾\n\nHow can I help you today? You can ask me to suggest recipes, check seasonal products, offer organic farming tips, or track your orders.";
    }

    // --- ORDER ASSISTANCE (Feature 15) ---
    private String getOrderAssistance(Long userId, String msg, String lang) {
        if (userId == null) {
            return lang.equals("marathi") ? "कृपया तुमची ऑर्डर माहिती पाहण्यासाठी लॉग इन करा." :
                   (lang.equals("hindi") ? "कृपया अपनी ऑर्डर जानकारी देखने के लिए लॉग इन करें।" : "Please log in to track or view your orders.");
        }

        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        if (orders.isEmpty()) {
            return lang.equals("marathi") ? "तुम्हाला कोणतीही ऑर्डर सापडली नाही. खरेदी करण्यासाठी कृपया शॉप वर जा!" :
                   (lang.equals("hindi") ? "हमें आपकी कोई ऑर्डर नहीं मिली। खरीदारी शुरू करने के लिए शॉप पेज पर जाएं।" : "We couldn't find any orders for your account yet. Visit the Shop to place one!");
        }

        // Try to identify a specific order ID in query
        Order targetOrder = null;
        for (Order o : orders) {
            if (msg.contains(String.valueOf(o.getId()))) {
                targetOrder = o;
                break;
            }
        }

        // Fallback to latest order
        if (targetOrder == null) {
            targetOrder = orders.get(0);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        String dateStr = targetOrder.getOrderDate().format(formatter);

        StringBuilder sb = new StringBuilder();
        if (lang.equals("marathi")) {
            sb.append("📦 **ऑर्डर ट्रॅकिंग आणि तपशील**:<br>");
        } else if (lang.equals("hindi")) {
            sb.append("📦 **ऑर्डर ट्रैकिंग और विवरण**:<br>");
        } else {
            sb.append("📦 **Order Assistance & Details**:<br>");
        }

        sb.append("<div class='card p-3 border-color shadow-sm bg-light my-2'>")
          .append("<div class='d-flex justify-content-between align-items-center mb-2'>")
          .append("<h6 class='fw-bold text-success mb-0'><i class='bi bi-truck me-2'></i>Order #").append(targetOrder.getId()).append("</h6>")
          .append("<span class='badge bg-success-subtle text-success'>").append(targetOrder.getStatus()).append("</span>")
          .append("</div>")
          .append("<p class='small text-muted mb-1'>Placed on: ").append(dateStr).append("</p>")
          .append("<p class='small mb-1'><strong>Total amount:</strong> ₹").append(targetOrder.getTotalAmount()).append("</p>")
          .append("<p class='small mb-1'><strong>Payment method:</strong> ").append(targetOrder.getPaymentMethod()).append(" (").append(targetOrder.getPaymentStatus()).append(")</p>")
          .append("<p class='small mb-2'><strong>Shipping address:</strong> ").append(targetOrder.getShippingAddress()).append("</p>")
          .append("<div class='d-grid gap-1'>")
          .append("<a href='tracking.html?orderId=").append(targetOrder.getId()).append("' class='btn btn-success btn-sm rounded-pill py-1.5 text-center' style='font-size: 11px;'>")
          .append("<i class='bi bi-geo-alt-fill me-1'></i> Live Order Tracking</a>")
          .append("</div>")
          .append("</div>");

        if (lang.equals("marathi")) {
            sb.append("<br>तुमची ऑर्डर सध्या **").append(targetOrder.getStatus()).append("** स्थितीमध्ये आहे. अधिक मदतीसाठी किंवा कॅन्सलेशनसाठी कृपया आमच्या सपोर्टशी संपर्क साधा.");
        } else if (lang.equals("hindi")) {
            sb.append("<br>आपकी ऑर्डर अभी **").append(targetOrder.getStatus()).append("** स्थिति में है। रद्दीकरण या अन्य सहायता के लिए ग्राहक सेवा से संपर्क करें।");
        } else {
            sb.append("<br>Your order is currently in **").append(targetOrder.getStatus()).append("** status. You can click the Live Tracking button above to track the delivery driver's real-time position!");
        }

        return sb.toString();
    }

    // --- RECIPE GENERATOR (Feature 1, 8, 9) ---
    private String handleRecipeQueries(String msg, String lang) {
        // 1. Check for specific pre-defined items for high-fidelity responses
        if (msg.contains("tomato") || msg.contains("टोमॅटो") || msg.contains("टमाटर")) {
            return getTomatoSoupRecipe(lang);
        } else if (msg.contains("breakfast") || msg.contains("नाश्ता")) {
            return getPohaRecipe(lang);
        } else if (msg.contains("protein") || msg.contains("कटलेट") || msg.contains("डाळ")) {
            return getHighProteinDaalRecipe(lang);
        } else if (msg.contains("diwali") || msg.contains("festival") || msg.contains("laddu") || msg.contains("गोड")) {
            return getBesanLadooRecipe(lang);
        }

        // 2. Extract ingredients from input (e.g., "potato, onion, tomato")
        List<String> userIngredients = new ArrayList<>();
        List<Product> allProducts = productRepository.findByStatus("ACTIVE");
        
        for (Product p : allProducts) {
            String name = p.getName().toLowerCase();
            if (msg.contains(name) || name.contains(msg)) {
                userIngredients.add(p.getName());
            }
        }
        
        // Fallback extraction for common kitchen items
        if (userIngredients.isEmpty()) {
            String[] commonItems = {"potato", "onion", "tomato", "milk", "rice", "apple", "mint", "banana", "spinach", "carrot", "cucumber"};
            for (String item : commonItems) {
                if (msg.contains(item)) {
                    userIngredients.add(item.substring(0, 1).toUpperCase() + item.substring(1));
                }
            }
        }

        // If ingredients are extracted, build a dynamic recipe card!
        if (!userIngredients.isEmpty()) {
            return generateDynamicRecipe(userIngredients, lang);
        }

        // 3. Default fallback
        return getMixedVegSaladRecipe(lang);
    }

    private String generateDynamicRecipe(List<String> ingredients, String lang) {
        String dishName = ingredients.stream().collect(Collectors.joining(", "));
        String title = lang.equals("marathi") ? dishName + " ची खमंग रेसिपी 🍲" :
                       (lang.equals("hindi") ? dishName + " की स्वादिष्ट रेसिपी 🍲" : "Custom Farm-Fresh " + dishName + " Recipe 🍲");
        
        String desc = lang.equals("marathi") ? "तुमच्या आवडीच्या घटकांपासून बनवलेली सोपी आणि चवदार घरगुती रेसिपी." :
                       (lang.equals("hindi") ? "आपके द्वारा चुने गए ताज़ा अवयवों से बनाई गई पौष्टिक और आसान रेसिपी।" : "A quick, nourishing, and tasty home-style recipe prepared using your chosen fresh crop ingredients.");

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='card border-color shadow-sm p-3 my-2'>")
          .append("<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>").append(title).append("</h5>")
          .append("<p class='text-muted small mb-2'>").append(desc).append("</p>")
          .append("<table class='table table-sm table-bordered mb-2' style='font-size: 11px;'>")
          .append("<tr><th>Prep Time</th><td>10 mins</td><th>Cook Time</th><td>15 mins</td></tr>")
          .append("<tr><th>Difficulty</th><td>Easy</td><th>Calories</th><td>180 kcal</td></tr>")
          .append("<tr><th>Protein</th><td>4.5g</td><th>Carbs</th><td>24g</td></tr>")
          .append("</table>")
          .append("<h6 class='fw-bold text-dark mb-1 small'>🛒 Ingredients Needed:</h6>")
          .append("<ul class='small mb-2' style='padding-left: 20px;'>");
        
        for (String ing : ingredients) {
            sb.append("<li>Fresh ").append(ing).append(" - as required</li>");
        }
        sb.append("<li>Chopped Onions & Garlic - 1 cup</li>")
          .append("<li>Spices (Salt, Turmeric, Chili) - to taste</li>")
          .append("<li>Cooking Oil or Ghee - 1 tbsp</li>")
          .append("</ul>")
          .append("<h6 class='fw-bold text-dark mb-1 small'>🍳 Preparation Steps:</h6>")
          .append("<ol class='small mb-2' style='padding-left: 20px;'>")
          .append("<li>Wash and chop the fresh ").append(ingredients.get(0).toLowerCase()).append(" and other vegetables into small pieces.</li>")
          .append("<li>Heat oil in a cooking pan, add cumin seeds, chopped onions, and garlic. Sauté until lightly golden.</li>")
          .append("<li>Add the spices (turmeric, salt, chili powder) and mix well.</li>")
          .append("<li>Add the chopped ingredients (").append(ingredients.stream().map(String::toLowerCase).collect(Collectors.joining(", "))).append(") into the pan. Stir cook for 5 mins.</li>")
          .append("<li>Add half cup of water, cover with a lid, and let it simmer on low heat for 10 minutes until tender.</li>")
          .append("<li>Garnish with coriander or mint leaves and serve hot with fresh flatbreads or rice!</li>")
          .append("</ol>")
          .append("<h6 class='fw-bold text-success mb-1 small'>Suggested Products from Website:</h6>")
          .append("<div class='d-flex flex-wrap gap-2 mt-2'>");

        List<Product> allProducts = productRepository.findByStatus("ACTIVE");
        int count = 0;
        for (String ing : ingredients) {
            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(ing.toLowerCase()) || ing.toLowerCase().contains(p.getName().toLowerCase())) {
                    sb.append("  <div class='border rounded p-2 text-center' style='width: 100px; font-size: 10px; background-color: #fff;'>")
                      .append("    <img src='").append(p.getImageUrl() != null ? p.getImageUrl() : "https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=100").append("' style='width:50px; height:50px; object-fit:cover;' class='rounded mb-1'><br>")
                      .append("    <strong>").append(p.getName()).append("</strong><br>₹").append(p.getPrice()).append("/").append(p.getUnit()).append("<br>")
                      .append("    <button onclick='quickAddToCart(").append(p.getId()).append(", 1)' class='btn btn-success btn-xs px-2 py-0.5 rounded-pill mt-1' style='font-size:9px;'>+ Cart</button>")
                      .append("  </div>");
                    count++;
                    break;
                }
            }
        }
        
        if (count == 0 && !allProducts.isEmpty()) {
            Product p = allProducts.get(0);
            sb.append("  <div class='border rounded p-2 text-center' style='width: 100px; font-size: 10px; background-color: #fff;'>")
              .append("    <img src='").append(p.getImageUrl() != null ? p.getImageUrl() : "https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=100").append("' style='width:50px; height:50px; object-fit:cover;' class='rounded mb-1'><br>")
              .append("    <strong>").append(p.getName()).append("</strong><br>₹").append(p.getPrice()).append("/").append(p.getUnit()).append("<br>")
              .append("    <button onclick='quickAddToCart(").append(p.getId()).append(", 1)' class='btn btn-success btn-xs px-2 py-0.5 rounded-pill mt-1' style='font-size:9px;'>+ Cart</button>")
              .append("  </div>");
        }

        sb.append("</div>")
          .append("</div>");

        return sb.toString();
    }

    private String getTomatoSoupRecipe(String lang) {
        boolean mr = lang.equals("marathi");
        boolean hi = lang.equals("hindi");

        String title = mr ? "टमॅटो सूप (Tomato Soup)" : (hi ? "टमाटर का सूप (Tomato Soup)" : "Classic Roasted Tomato Soup 🍅");
        String desc = mr ? "एक सोपे, निरोगी आणि चवदार सूप जे पचन सुधारते." : (hi ? "एक स्वादिष्ट, आसान और पौष्टिक सूप जो सर्दियों और मानसून में बेहतरीन है।" : "A rich, creamy, and warm soup made with farm-fresh organic tomatoes.");

        return "<div class='card border-color shadow-sm p-3 my-2'>" +
                "<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>" + title + "</h5>" +
                "<p class='text-muted small mb-2'>" + desc + "</p>" +
                "<table class='table table-sm table-bordered mb-2' style='font-size: 11px;'>" +
                "<tr><th>Prep Time</th><td>10 mins</td><th>Cook Time</th><td>20 mins</td></tr>" +
                "<tr><th>Difficulty</th><td>Easy</td><th>Calories</th><td>120 kcal</td></tr>" +
                "<tr><th>Protein</th><td>2.5g</td><th>Carbs</th><td>12g</td></tr>" +
                "</table>" +
                "<h6 class='fw-bold text-dark mb-1 small'>🛒 Ingredients Needed:</h6>" +
                "<ul class='small mb-2' style='padding-left: 20px;'>" +
                "<li>Fresh Red Tomatoes - 500g</li>" +
                "<li>Onion (Bar बारीक चिरलेला) - 1 small</li>" +
                "<li>Garlic cloves - 3</li>" +
                "<li>Olive oil or butter - 1 tbsp</li>" +
                "<li>Salt & Black Pepper - to taste</li>" +
                "</ul>" +
                "<h6 class='fw-bold text-dark mb-1 small'>🍳 Step-by-Step cooking:</h6>" +
                "<ol class='small mb-2' style='padding-left: 20px;'>" +
                "<li>Sauté onion and garlic in butter until soft.</li>" +
                "<li>Add chopped fresh tomatoes, salt, and 1 cup of water. Simmer for 15 mins.</li>" +
                "<li>Let it cool down, blend it into a smooth purée and strain.</li>" +
                "<li>Reheat, season with fresh black pepper and serve hot with cream.</li>" +
                "</ol>" +
                "<h6 class='fw-bold text-success mb-1 small'>Suggested Products from Website:</h6>" +
                "<div class='d-flex gap-2 mt-2'>" +
                "  <div class='border rounded p-2 text-center' style='width: 100px; font-size: 10px; background-color: #fff;'>" +
                "    <img src='https://images.unsplash.com/photo-1595855759920-86582396756a?w=100' style='width:50px; height:50px; object-fit:cover;' class='rounded mb-1'><br>" +
                "    <strong>Tomatoes</strong><br>₹40/kg<br>" +
                "    <button onclick='quickAddToCart(1, 1)' class='btn btn-success btn-xs px-2 py-0.5 rounded-pill mt-1' style='font-size:9px;'>+ Cart</button>" +
                "  </div>" +
                "  <div class='border rounded p-2 text-center' style='width: 100px; font-size: 10px; background-color: #fff;'>" +
                "    <img src='https://images.unsplash.com/photo-1618220179428-22790b461013?w=100' style='width:50px; height:50px; object-fit:cover;' class='rounded mb-1'><br>" +
                "    <strong>Mint Leaves</strong><br>₹15/bndl<br>" +
                "    <button onclick='quickAddToCart(4, 1)' class='btn btn-success btn-xs px-2 py-0.5 rounded-pill mt-1' style='font-size:9px;'>+ Cart</button>" +
                "  </div>" +
                "</div>" +
                "</div>";
    }

    private String getPohaRecipe(String lang) {
        return "<div class='card border-color shadow-sm p-3 my-2'>" +
                "<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>Kanda Poha 🌾</h5>" +
                "<p class='text-muted small mb-2'>A light, fluffy, and highly popular Indian breakfast made with flattened rice and fresh onions.</p>" +
                "<table class='table table-sm table-bordered mb-2' style='font-size: 11px;'>" +
                "<tr><th>Prep Time</th><td>5 mins</td><th>Cook Time</th><td>10 mins</td></tr>" +
                "<tr><th>Difficulty</th><td>Easy</td><th>Calories</th><td>250 kcal</td></tr>" +
                "</table>" +
                "<h6 class='fw-bold text-dark mb-1 small'>🍳 Preparation Steps:</h6>" +
                "<ol class='small mb-2' style='padding-left: 20px;'>" +
                "<li>Rinse poha under water until damp and soft. Drain completely.</li>" +
                "<li>Heat oil, add mustard seeds, curry leaves, green chilies, and peanuts.</li>" +
                "<li>Add finely chopped onions and sauté until translucent. Add turmeric.</li>" +
                "<li>Mix in the poha, sprinkle salt, cover and steam for 3 mins. Top with lemon juice and coriander.</li>" +
                "</ol>" +
                "</div>";
    }

    private String getHighProteinDaalRecipe(String lang) {
        return "<div class='card border-color shadow-sm p-3 my-2'>" +
                "<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>High-Protein Mixed Lentil Daal 🍲</h5>" +
                "<p class='text-muted small mb-2'>A protein-dense, vegan dish packed with Indian spices and premium split lentils.</p>" +
                "<table class='table table-sm table-bordered mb-2' style='font-size: 11px;'>" +
                "<tr><th>Protein</th><td>18g</td><th>Calories</th><td>310 kcal</td><th>Difficulty</th><td>Medium</td></tr>" +
                "</table>" +
                "</div>";
    }

    private String getBesanLadooRecipe(String lang) {
        return "<div class='card border-color shadow-sm p-3 my-2'>" +
                "<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>Festival Special: Besan Ladoo 🍬</h5>" +
                "<p class='text-muted small mb-2'>A classic sweet treat prepared during Diwali and Ganesh festivals using roasted gram flour, ghee, and cardamom.</p>" +
                "</div>";
    }

    private String getAlooKormaRecipe(String lang) {
        return "<div class='card border-color shadow-sm p-3 my-2'>" +
                "<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>Spicy Alloo Korma 🥔</h5>" +
                "<p class='text-muted small mb-2'>A quick curry generated using your custom ingredients: potatoes, onions, and tomatoes.</p>" +
                "</div>";
    }

    private String getMixedVegSaladRecipe(String lang) {
        return "<div class='card border-color shadow-sm p-3 my-2'>" +
                "<h5 class='fw-bold text-success mb-1'><i class='bi bi-egg-fried me-2'></i>Farm Fresh Mixed Green Salad 🥗</h5>" +
                "<p class='text-muted small mb-2'>A quick, raw, and low-calorie recipe composed of chopped cucumbers, tomatoes, carrots, and mint leaves dressing.</p>" +
                "</div>";
    }

    // --- SEASONAL PRODUCE (Feature 2) ---
    private String getSeasonalProduce(String msg, String lang) {
        boolean mr = lang.equals("marathi");
        boolean hi = lang.equals("hindi");

        StringBuilder sb = new StringBuilder();
        if (mr) {
            sb.append("📅 **चालू महिन्यातील हंगामी पिके व फळे (July - पावसाळी हंगाम)**:<br><br>")
              .append("**भाज्या (Vegetables)**:<br>")
              .append("• **भेंडी (Okra)** - पचनासाठी उत्तम, व्हिटॅमिन सी ने समृद्ध.<br>")
              .append("• **दुधी भोपळा (Bottle Gourd)** - थंड प्रभाव, वजन कमी करण्यासाठी उत्तम.<br>")
              .append("• **कारले (Bitter Gourd)** - रक्तातील साखरेचे नियंत्रण करते.<br><br>")
              .append("**फळे (Fruits)**:<br>")
              .append("• **जांभूळ (Jamun)** - रक्तातील साखर कमी करते.<br>")
              .append("• **पपई (Papaya)** - व्हिटॅमिन ए आणि फायबर समृद्ध.<br>")
              .append("• **केळी (Banana)** - पोटॅशियमचा उत्तम स्त्रोत.");
        } else if (hi) {
            sb.append("📅 **इस महीने की मौसमी सब्जियां और फल (July - मानसून)**:<br><br>")
              .append("**सब्जियां (Vegetables)**:<br>")
              .append("• **भिंडी (Okra)** - पाचन के लिए बहुत अच्छी है।<br>")
              .append("• **लौकी (Bottle Gourd)** - शरीर को ठंडा रखती है, वजन कम करती है।<br>")
              .append("• **करेला (Bitter Gourd)** - मधुमेह रोगियों के लिए अत्यंत स्वास्थ्यवर्धक।<br><br>")
              .append("**फल (Fruits)**:<br>")
              .append("• **जामुन (Jamun)** - शुगर लेवल कंट्रोल करता है।<br>")
              .append("• **पपीता (Papaya)** - पाचन क्रिया दुरुस्त करता है।");
        } else {
            sb.append("📅 **Seasonal Recommendations (Current Month: July)**:<br><br>")
              .append("<table class='table table-sm table-striped' style='font-size:11px;'>")
              .append("<thead><tr class='bg-success text-white'><th>Produce</th><th>Benefits</th><th>Market Price</th></tr></thead>")
              .append("<tbody>")
              .append("<tr><td>🥦 Okra (Lady Finger)</td><td>High fiber, good for heart</td><td>₹35 / kg</td></tr>")
              .append("<tr><td>🥒 Bottle Gourd</td><td>Hydrating, high water %</td><td>₹25 / kg</td></tr>")
              .append("<tr><td>🍠 Bitter Gourd</td><td>Regulates sugar</td><td>₹40 / kg</td></tr>")
              .append("<tr><td>🥭 Papaya</td><td>Vitamin C & digestion</td><td>₹60 / kg</td></tr>")
              .append("<tr><td>🍌 Banana</td><td>Instant energy & potassium</td><td>₹50 / doz</td></tr>")
              .append("</tbody></table>")
              .append("<br>💡 **Freshness Tip**: Choose firm okra that snaps easily at the tip. Store greens in a dry, ventilated box in your refrigerator.");
        }

        return sb.toString();
    }

    // --- HEALTHY ADVISOR & NUTRITION (Feature 3, 6, 14) ---
    private String handleNutritionAndHealth(String msg, String lang) {
        StringBuilder sb = new StringBuilder();

        if (msg.contains("diabet") || msg.contains("साखर") || msg.contains("मधुमेह")) {
            sb.append("🥗 **Diabetes Diet Advisor**:<br>")
              .append("✔ **Best Foods**: Green leafy vegetables, Fenugreek (Methi) seeds, bitter gourd, whole oats, whole pulses, and wheat bran.<br>")
              .append("❌ **Foods to Avoid**: Refined flour (Maida), white sugar, sweet mangoes, canned juices, sodas, and potatoes.<br>")
              .append("💡 **Daily Intake**: Aim for at least 30g of dietary fiber daily.<br><br>")
              .append("⚠️ *Medical Disclaimer: Please consult a registered medical doctor or certified clinical dietitian before making significant changes.*");
        } else if (msg.contains("gym") || msg.contains("protein") || msg.contains("स्नायू")) {
            sb.append("💪 **Gym Diet / High-Protein Advisory**:<br>")
              .append("✔ **Top Veg Sources**: Soybeans, cottage cheese (paneer), chickpeas, green peas, high-protein grains, and lentils.<br>")
              .append("✔ **Top Fruits**: Avocados, bananas (post-workout carb replenishing).<br>")
              .append("💡 **Suggested Intake**: 1.2g to 1.6g of protein per kg of bodyweight daily for active muscle development.");
        } else if (msg.contains("weight loss") || msg.contains("वजन")) {
            sb.append("🏃 **Weight Loss Tips**:<br>")
              .append("• Replace white rice with high-fiber grains like brown rice or millet.<br>")
              .append("• Consume raw cucumbers and tomatoes before lunch to naturally limit portion sizes.<br>")
              .append("• Drink at least 3.5 litres of fresh water daily.");
        } else {
            // General Nutrition breakdown
            sb.append("🍎 **General Nutrition Breakdown (Nutrition Expert)**:<br><br>")
              .append("<table class='table table-sm table-bordered' style='font-size:11px;'>")
              .append("<tr class='table-success'><th colspan='2'>Per 100g of fresh Greens</th></tr>")
              .append("<tr><td>Water Percentage</td><td>92%</td></tr>")
              .append("<tr><td>Calories</td><td>22 kcal</td></tr>")
              .append("<tr><td>Dietary Fiber</td><td>2.8g</td></tr>")
              .append("<tr><td>Protein</td><td>1.2g</td></tr>")
              .append("<tr><td>Vitamin C</td><td>25% of DRI</td></tr>")
              .append("<tr><td>Calcium</td><td>4% of DRI</td></tr>")
              .append("</table>");
        }

        return sb.toString();
    }

    // --- FARMING KNOWLEDGE (Feature 7) ---
    private String getFarmingKnowledge(String msg, String lang) {
        StringBuilder sb = new StringBuilder();

        if (msg.contains("tomato") || msg.contains("टोमॅटो") || msg.contains("टमाटर")) {
            sb.append("🍅 **Farming Guide: How to grow Tomatoes**:<br><br>")
              .append("1. **Sowing Time**: Best sown during October-November (winter crop) or June-July (monsoon crop).<br>")
              .append("2. **Soil Conditions**: Requires well-drained loamy soil with a pH range of 6.0 to 7.0.<br>")
              .append("3. **Water Management**: Implement drip irrigation. Water deeply once in 3 days rather than daily shallow waterings.<br>")
              .append("4. **Fertilizers**: Apply well-decomposed cow dung compost during land preparation. Use potassium-rich fertilizers during fruit setting.");
        } else if (msg.contains("pest") || msg.contains("किड")) {
            sb.append("🐜 **Organic Pest Control Advisory**:<br><br>")
              .append("• **Neem Oil Spray**: Mix 15ml of organic cold-pressed neem oil with 2-3 drops of liquid dish soap in 1 litre of water. Spray once a week after sunset.<br>")
              .append("• **Yellow Sticky Traps**: Place yellow sticky cards to capture flying whiteflies, leafminers, and aphids naturally without chemicals.");
        } else {
            sb.append("🌾 **Farmer Advisory & Agro-Tech Knowledge**:<br><br>")
              .append("• **Drip Irrigation**: Saves up to 60% water consumption compared to flood irrigation, reduces weed growth, and deposits fertilizers directly at root levels.<br>")
              .append("• **Composting**: Prepare compost using a 3:1 ratio of dry leaves (browns) and green kitchen scraps (greens) in a compost bin. Maintain moisture and turn weekly.");
        }

        return sb.toString();
    }

    // --- PRODUCT SEARCH & SHOPPING ASSISTANT (Feature 4, 5, 13) ---
    private String handleProductAndSearch(String msg, String lang) {
        // Query products from database dynamically
        List<Product> allProducts = productRepository.findByStatus("ACTIVE");
        List<Product> matches = new ArrayList<>();

        // Fuzzy checking
        for (Product p : allProducts) {
            String name = p.getName().toLowerCase();
            String cat = p.getCategory().getName().toLowerCase();
            
            // Check for spelling errors / substrings
            if (msg.contains(name) || name.contains(msg) ||
                msg.contains(cat) || cat.contains(msg) ||
                (msg.contains("soup") && (name.contains("tomato") || name.contains("onion") || name.contains("potato") || name.contains("mint"))) ||
                (msg.contains("curry") && (name.contains("rice") || name.contains("tomato") || name.contains("potato") || name.contains("onion")))) {
                matches.add(p);
            }
        }

        // Limit results to 4
        if (matches.size() > 4) {
            matches = matches.subList(0, 4);
        }

        if (matches.isEmpty()) {
            // Default response if no database product matches
            if (lang.equals("marathi")) {
                return "मला त्या विषयाबद्दल थेट उत्पादन सापडले नाही, परंतु मी एक कृषी सहाय्यक म्हणून मदत करू शकतो. तुम्ही 'रेसिपी', 'ऑर्डर स्टेटस' किंवा 'हंगामी पिके' याबद्दल विचारू शकता.";
            } else if (lang.equals("hindi")) {
                return "मुझे डेटाबेस में आपकी पसंद का उत्पाद नहीं मिला। कृपया अलग नाम जैसे 'टमाटर', 'दूध', किंवा 'चावल' लिखकर सर्च करें।";
            }
            return "I couldn't find any products in our database directly matching your keyword. Try asking for specific items like: **tomatoes, potatoes, milk, rice, apples**, or browse the **Shop** page!";
        }

        StringBuilder sb = new StringBuilder();
        if (lang.equals("marathi")) {
            sb.append("🛒 **आमच्या जवळील ताजी उत्पादने**:<br>");
        } else if (lang.equals("hindi")) {
            sb.append("🛒 **आपके लिए ताज़ा उत्पाद**:<br>");
        } else {
            sb.append("🛒 **Recommended Farm-Fresh Products**:<br>");
        }

        sb.append("<div class='row row-cols-1 row-cols-md-2 g-2 my-2'>");
        for (Product p : matches) {
            String statusBadge = p.getQuantity() > 10 ? "Organic" : "Non-organic";
            String rating = "4.5 ★";

            sb.append("<div class='col'>")
              .append("<div class='card h-100 border-color shadow-sm' style='background-color: var(--card-bg);'>")
              .append("<img src='").append(p.getImageUrl() != null ? p.getImageUrl() : "https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=300").append("' class='card-img-top object-fit-cover' style='height: 100px;' alt='").append(p.getName()).append("'>")
              .append("<div class='card-body p-2 d-flex flex-column justify-content-between'>")
              .append("<div>")
              .append("<span class='badge bg-success-subtle text-success' style='font-size: 8px;'>").append(p.getCategory().getName()).append("</span>")
              .append("<h6 class='fw-bold mb-0 mt-1 small text-dark'>").append(p.getName()).append("</h6>")
              .append("<small class='text-muted d-block' style='font-size: 9px;'>Farmer: ").append(p.getFarmer().getName()).append("</small>")
              .append("<small class='text-success fw-bold' style='font-size: 10px;'>").append(statusBadge).append(" | ").append(rating).append("</small>")
              .append("</div>")
              .append("<div class='mt-2'>")
              .append("<p class='fw-bold text-success mb-1 small'>₹").append(p.getPrice()).append(" / ").append(p.getUnit()).append("</p>")
              .append("<button onclick=\"quickAddToCart(").append(p.getId()).append(", 1)\" class='btn btn-success btn-sm w-100 py-1.5 rounded-pill' style='font-size: 10px;' ")
              .append(p.getQuantity() == 0 ? "disabled" : "").append(">")
              .append("<i class='bi bi-cart-plus me-1'></i>Add to Cart</button>")
              .append("</div>")
              .append("</div>")
              .append("</div>")
              .append("</div>");
        }
        sb.append("</div>");

        return sb.toString();
    }

    public List<Product> getRecommendations(Long userId) {
        return productRepository.findByStatus("ACTIVE").stream()
                .limit(4)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDemandPrediction() {
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("lastUpdated", java.time.LocalDateTime.now().toString());
        
        List<Map<String, Object>> cropTrends = new ArrayList<>();
        cropTrends.add(createTrend("Organic Tomatoes", "HIGH DEMAND", "Price expected to rise by 15% due to high kitchen consumption and low summer supply.", 9.2));
        cropTrends.add(createTrend("Pure Cow Milk", "STABLE", "Consistent year-round demand. Supply matches consumption.", 5.0));
        cropTrends.add(createTrend("Basmati Rice", "MODERATE", "Stable demand. Good stock availability from Haryana farmers.", 6.5));
        cropTrends.add(createTrend("Green Apples", "HIGH DEMAND", "High request for fresh fruits. Harvest from Himachal is awaited.", 8.7));
        cropTrends.add(createTrend("Farm Fresh Potatoes", "LOW DEMAND", "Large cold storage stock available, prices expected to dip by 5%.", 3.2));

        prediction.put("trends", cropTrends);
        prediction.put("generalAdvice", "Farmers should prioritize quick-harvesting salad greens and organic tomatoes, as local city margins are currently at an all-time high.");
        return prediction;
    }

    private Map<String, Object> createTrend(String crop, String status, String description, double score) {
        Map<String, Object> trend = new HashMap<>();
        trend.put("cropName", crop);
        trend.put("status", status);
        trend.put("analysis", description);
        trend.put("demandScore", score);
        return trend;
    }

    private String estimateBill(String msg, String lang) {
        List<Product> allProducts = productRepository.findByStatus("ACTIVE");
        List<Map<String, Object>> estimatedItems = new ArrayList<>();
        double grandTotal = 0;

        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:kg|doz|dozen|unit|pkt|packet)?s?\\s+([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(msg);

        while (matcher.find()) {
            double qty = Double.parseDouble(matcher.group(1));
            String nameQuery = matcher.group(2).toLowerCase();

            Product bestMatch = null;
            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(nameQuery) || nameQuery.contains(p.getName().toLowerCase())) {
                    bestMatch = p;
                    break;
                }
            }

            if (bestMatch != null) {
                double subtotal = qty * bestMatch.getPrice();
                grandTotal += subtotal;

                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("product", bestMatch);
                itemMap.put("qty", qty);
                itemMap.put("subtotal", subtotal);
                estimatedItems.add(itemMap);
            }
        }

        if (estimatedItems.isEmpty()) {
            if (lang.equals("marathi")) {
                return "मला खरेदी बिल मोजण्यासाठी कोणतेही स्पष्ट प्रमाण सापडले नाही (उदा. '2 kg tomato चे बिल सांगा').";
            } else if (lang.equals("hindi")) {
                return "मुझे बिल का अनुमान लगाने के लिए कोई मात्रा नहीं मिली (उदा. '2 kg tomato का बिल बताएं')।";
            }
            return "I couldn't detect any specific products or quantities to estimate the bill. Try asking like: **'estimate bill for 2 kg tomatoes and 1 kg potato'**!";
        }

        StringBuilder sb = new StringBuilder();
        if (lang.equals("marathi")) {
            sb.append("🛒 **अंदाजे खरेदी बिल (Shopping Assistant)**:<br>");
        } else if (lang.equals("hindi")) {
            sb.append("🛒 **अनुमानित खरीदारी बिल (Shopping Assistant)**:<br>");
        } else {
            sb.append("🛒 **Estimated Shopping Bill & Breakdown**:<br>");
        }

        sb.append("<div class='card p-3 border-color shadow-sm bg-light my-2'>")
          .append("<table class='table table-sm' style='font-size:11.5px;'>")
          .append("<thead><tr><th>Product</th><th>Qty</th><th>Rate</th><th>Subtotal</th></tr></thead>")
          .append("<tbody>");

        StringBuilder jsonArray = new StringBuilder("[");
        for (int i = 0; i < estimatedItems.size(); i++) {
            Map<String, Object> item = estimatedItems.get(i);
            Product p = (Product) item.get("product");
            double qty = (double) item.get("qty");
            double subtotal = (double) item.get("subtotal");

            sb.append("<tr><td>").append(p.getName()).append("</td>")
              .append("<td>").append(qty).append(" ").append(p.getUnit()).append("</td>")
              .append("<td>₹").append(p.getPrice()).append("</td>")
              .append("<td><strong>₹").append(subtotal).append("</strong></td></tr>");

            jsonArray.append("{\"id\":").append(p.getId()).append(",\"qty\":").append(qty).append("}");
            if (i < estimatedItems.size() - 1) {
                jsonArray.append(",");
            }
        }
        jsonArray.append("]");

        sb.append("</tbody></table>")
          .append("<hr class='my-2'>")
          .append("<div class='d-flex justify-content-between align-items-center mb-2'>")
          .append("<h6 class='fw-bold mb-0'>Grand Total:</h6>")
          .append("<h5 class='fw-bold text-success mb-0'>₹").append(grandTotal).append("</h5>")
          .append("</div>")
          .append("<button onclick='addMultipleToCart(").append(jsonArray.toString()).append(")' class='btn btn-success btn-sm w-100 rounded-pill py-1.5' style='font-size:11px;'>")
          .append("<i class='bi bi-cart-plus me-1'></i> Add All to Cart")
          .append("</button>")
          .append("</div>");

        return sb.toString();
    }

    private String getDailyHealthTips(String lang) {
        boolean mr = lang.equals("marathi");
        boolean hi = lang.equals("hindi");

        if (mr) {
            return "💧 **स्मार्ट आरोग्य आणि आहार सल्ला**:<br><br>" +
                    "• **दिवसाचे फळ (Fruit of the Day)**: डाळिंब (Pomegranate) - रक्ताभिसरण आणि रोगप्रतिकारशक्ती वाढवते.<br>" +
                    "• **दिवसाची भाजी (Vegetable of the Day)**: मेथी (Fenugreek) - रक्तातील साखर नियंत्रित ठेवण्यास मदत करते.<br>" +
                    "• **हायड्रेशन रिमाइंडर**: निरोगी राहण्यासाठी आज किमान ३.५ लिटर पाणी प्या! 🥤<br>" +
                    "• **आरोग्यदायी कृती**: पुदिना आणि लिंबू पाणी प्या जे पचनक्रिया गतिमान करते.";
        } else if (hi) {
            return "💧 **दैनिक स्वास्थ्य और पोषण सलाह**:<br><br>" +
                    "• **आज का फल (Fruit of the Day)**: अनार (Pomegranate) - खून बढ़ाने और हृदय के लिए अत्यंत गुणकारी।<br>" +
                    "• **आज की सब्जी (Vegetable of the Day)**: पालक (Spinach) - आयरन और कैल्शियम का बेहतरीन स्रोत।<br>" +
                    "• **पानी पीने की सलाह**: आज कम से कम ८ गिलास ताज़ा पानी अवश्य पिएं! 🥤<br>" +
                    "• **स्वास्थ्य नुस्खा**: सुबह खाली पेट ताज़ा नींबू पानी पीने से पाचन क्रिया सुधरती है।";
        }

        return "💧 **Smart Health & Hydration Assistant**:<br><br>" +
                "<div class='card p-3 border-color bg-light shadow-sm my-2'>" +
                "  <h6 class='fw-bold text-success mb-2'><i class='bi bi-heartpulse-fill me-2'></i>Advisory of the Day</h6>" +
                "  <ul class='small mb-2' style='padding-left: 20px;'>" +
                "    <li><strong>Fruit of the Day</strong>: Pomegranate - Enhances blood circulation & immunity.</li>" +
                "    <li><strong>Vegetable of the Day</strong>: Organic Spinach - Rich in Iron, Calcium & dietary fibers.</li>" +
                "    <li><strong>Hydration Reminder</strong>: Drink at least 3.5 Litres of fresh water today! 💧</li>" +
                "    <li><strong>Recipe of the Day</strong>: Roasted Mint & Spinach Clear Soup.</li>" +
                "  </ul>" +
                "  <div class='alert alert-warning p-2 mb-0 mt-2' style='font-size:10.5px; border:none;'>" +
                "    ⚠️ *Disclaimer: Suggested health tips are for informational purpose. Consult a doctor for any specific conditions.*" +
                "  </div>" +
                "</div>";
    }
}
