// F2C Global Application Configuration and Utility Script

const API_BASE = ""; // Relative paths since we serve statically from Spring Boot

function getRelativePrefix() {
    const path = window.location.pathname;
    if (path.includes("/customer/") || path.includes("/farmer/") || path.includes("/admin/")) {
        return "../";
    }
    return "";
}

// Token & Auth Storage Helpers
function getAuthToken() {
    return localStorage.getItem("f2c_token");
}

function setAuthToken(token) {
    localStorage.setItem("f2c_token", token);
}

function getCurrentUser() {
    const userStr = localStorage.getItem("f2c_user");
    return userStr ? JSON.parse(userStr) : null;
}

function setCurrentUser(user) {
    localStorage.setItem("f2c_user", JSON.stringify(user));
}

function logout() {
    localStorage.removeItem("f2c_token");
    localStorage.removeItem("f2c_user");
    window.location.href = getRelativePrefix() + "login.html";
}

// Global API Fetch wrapper with automatic JWT injection
async function apiFetch(endpoint, options = {}) {
    const token = getAuthToken();
    
    // Set headers
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };
    
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    
    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);
        
        // Handle unauthorized (expired/missing token)
        if (response.status === 401) {
            localStorage.removeItem("f2c_token");
            localStorage.removeItem("f2c_user");
            window.location.href = getRelativePrefix() + "login.html";
            throw new Error("Session expired. Please login again.");
        }
        
        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || "API Request failed");
        }
        
        // Return JSON if present, otherwise text
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return await response.json();
        }
        return await response.text();
    } catch (error) {
        console.error(`API Fetch Error [${endpoint}]:`, error);
        throw error;
    }
}

// Dark Mode Toggle
function initTheme() {
    const theme = localStorage.getItem("theme") || "light";
    if (theme === "dark") {
        document.body.classList.add("dark-mode");
    }
    
    // Bind buttons with id 'theme-toggle'
    document.querySelectorAll(".theme-toggle").forEach(btn => {
        btn.addEventListener("click", () => {
            document.body.classList.toggle("dark-mode");
            const newTheme = document.body.classList.contains("dark-mode") ? "dark" : "light";
            localStorage.setItem("theme", newTheme);
            updateThemeIcons();
        });
    });
    updateThemeIcons();
}

function updateThemeIcons() {
    const isDark = document.body.classList.contains("dark-mode");
    document.querySelectorAll(".theme-toggle").forEach(btn => {
        btn.innerHTML = isDark ? '<i class="bi bi-sun-fill text-warning"></i>' : '<i class="bi bi-moon-fill text-secondary"></i>';
    });
}

// Global Translation Dictionary
const translations = {
    en: {
        welcome_title: "Fresh From Farm To Your Doorstep",
        hero_desc: "Direct marketplace connecting hardworking local farmers with families who love nutritious, natural food. Skip the middlemen!",
        shop_btn: "Shop Fresh Produce",
        farmer_btn: "Join as Farmer",
        categories_title: "Browse categories",
        notifications_title: "Notifications",
        mark_all_read: "Mark all as read",
        no_notifications: "No notifications.",
        sign_in: "Sign In",
        register: "Register",
        logout: "Logout",
        home: "Home",
        shop: "Shop",
        cart: "Cart",
        wishlist: "Wishlist",
        checkout: "Checkout",
        orders: "Orders",
        profile: "Profile",
        vegetables: "Vegetables",
        fruits: "Fruits",
        search_placeholder: "Search fresh produce...",
        "notification.order.placed.title": "Order Placed Successfully",
        "notification.order.placed.message": "Your order #{orderId} has been placed successfully!",
        "notification.order.confirmed.title": "Order Confirmed",
        "notification.order.confirmed.message": "Your order #{orderId} has been confirmed by the farmer.",
        "notification.order.shipped.title": "Order Shipped",
        "notification.order.shipped.message": "Your order #{orderId} has been shipped!",
        "notification.order.delivered.title": "Order Delivered",
        "notification.order.delivered.message": "Your order #{orderId} has been delivered successfully. Enjoy!",
        "notification.order.cancelled.title": "Order Cancelled",
        "notification.order.cancelled.message": "Your order #{orderId} has been cancelled.",
        "notification.farmer.new_order.title": "New Order Received",
        "notification.farmer.new_order.message": "You received a new order #{orderId}.",
        "notification.farmer.order_cancelled.title": "Order Cancelled",
        "notification.farmer.order_cancelled.message": "Order #{orderId} has been cancelled by customer.",
        chatbot_welcome: "Hello! I am your F2C Smart Assistant. How can I help you today? 😊"
    },
    mr: {
        welcome_title: "थेट शेतातून आपल्या दारापर्यंत",
        hero_desc: "कष्टाळू स्थानिक शेतकरी आणि पौष्टिक, नैसर्गिक अन्न आवडणाऱ्या कुटुंबांना जोडणारी थेट बाजारपेठ. मध्यस्थांना टाळा!",
        shop_btn: "ताजी उत्पादने खरेदी करा",
        farmer_btn: "शेतकरी म्हणून सामील व्हा",
        categories_title: "श्रेण्या निवडा",
        notifications_title: "सूचना",
        mark_all_read: "सर्व वाचलेले म्हणून चिन्हांकित करा",
        no_notifications: "कोणत्याही सूचना नाहीत.",
        sign_in: "लॉगिन करा",
        register: "नोंदणी करा",
        logout: "बाहेर पडा",
        home: "मुख्यपृष्ठ",
        shop: "खरेदी",
        cart: "कार्ट",
        wishlist: "इच्छासूची",
        checkout: "चेकआउट",
        orders: "ऑर्डर्स",
        profile: "प्रोफाइल",
        vegetables: "भाज्या",
        fruits: "फळे",
        search_placeholder: "ताजी उत्पादने शोधा...",
        "notification.order.placed.title": "ऑर्डर यशस्वीरित्या लागली",
        "notification.order.placed.message": "तुमचा ऑर्डर #{orderId} यशस्वीरित्या लागला आहे!",
        "notification.order.confirmed.title": "ऑर्डर निश्चित झाली",
        "notification.order.confirmed.message": "तुमचा ऑर्डर #{orderId} शेतकऱ्याने स्वीकारला आहे.",
        "notification.order.shipped.title": "ऑर्डर पाठवली गेली",
        "notification.order.shipped.message": "तुमचा ऑर्डर #{orderId} पाठवला गेला आहे!",
        "notification.order.delivered.title": "ऑर्डर पोहोचली",
        "notification.order.delivered.message": "तुमचा ऑर्डर #{orderId} यशस्वीरित्या पोहोचला आहे. आनंद घ्या!",
        "notification.order.cancelled.title": "ऑर्डर रद्द झाली",
        "notification.order.cancelled.message": "तुमचा ऑर्डर #{orderId} रद्द करण्यात आला आहे.",
        "notification.farmer.new_order.title": "नवीन ऑर्डर मिळाली",
        "notification.farmer.new_order.message": "तुम्हाला नवीन ऑर्डर #{orderId} मिळाली आहे.",
        "notification.farmer.order_cancelled.title": "ऑर्डर रद्द करण्यात आली",
        "notification.farmer.order_cancelled.message": "ऑर्डर #{orderId} ग्राहकाद्वारे रद्द करण्यात आली आहे.",
        chatbot_welcome: "नमस्कार! मी तुमचा F2C स्मार्ट असिस्टंट आहे. मी तुम्हाला कशी मदत करू शकतो? 😊"
    },
    hi: {
        welcome_title: "सीधे खेत से आपके घर तक",
        hero_desc: "मेहनती स्थानीय किसानों और पौष्टिक, प्राकृतिक भोजन पसंद करने वाले परिवारों को जोड़ने वाला सीधा बाजार। बिचौलियों से बचें!",
        shop_btn: "ताज़ा उत्पाद खरीदें",
        farmer_btn: "किसान के रूप में जुड़ें",
        categories_title: "श्रेणी ब्राउज़ करें",
        notifications_title: "सूचनाएं",
        mark_all_read: "सभी को पढ़ा हुआ मानें",
        no_notifications: "कोई सूचना नहीं।",
        sign_in: "लॉग इन करें",
        register: "पंजीकरण करें",
        logout: "लॉग आउट",
        home: "मुख्यपृष्ठ",
        shop: "दुकान",
        cart: "कार्ट",
        wishlist: "इच्छासूची",
        checkout: "चेकआउट",
        orders: "ऑर्डर्स",
        profile: "प्रोफाइल",
        vegetables: "सब्जियां",
        fruits: "फल",
        search_placeholder: "ताजे उत्पाद खोजें...",
        "notification.order.placed.title": "ऑर्डर सफलतापूर्वक सबमिट हुआ",
        "notification.order.placed.message": "आपका ऑर्डर #{orderId} सफलतापूर्वक सबमिट हो गया है!",
        "notification.order.confirmed.title": "ऑर्डर की पुष्टि हो गई",
        "notification.order.confirmed.message": "आपका ऑर्डर #{orderId} किसान द्वारा स्वीकार कर लिया गया है।",
        "notification.order.shipped.title": "ऑर्डर भेज दिया गया",
        "notification.order.shipped.message": "आपका ऑर्डर #{orderId} भेज दिया गया है!",
        "notification.order.delivered.title": "ऑर्डर वितरित हो गया",
        "notification.order.delivered.message": "आपका ऑर्डर #{orderId} सफलतापूर्वक वितरित हो गया है। आनंद लें!",
        "notification.order.cancelled.title": "ऑर्डर रद्द कर दिया गया",
        "notification.order.cancelled.message": "आपका ऑर्डर #{orderId} रद्द कर दिया गया है।",
        "notification.farmer.new_order.title": "नया ऑर्डर प्राप्त हुआ",
        "notification.farmer.new_order.message": "आपको एक नया ऑर्डर #{orderId} प्राप्त हुआ है।",
        "notification.farmer.order_cancelled.title": "ऑर्डर रद्द कर दिया गया",
        "notification.farmer.order_cancelled.message": "ऑर्डर #{orderId} ग्राहक द्वारा रद्द कर दिया गया है।",
        chatbot_welcome: "नमस्ते! मैं आपका F2C स्मार्ट असिस्टेंट हूँ। आज मैं आपकी क्या मदद कर सकता हूँ? 😊"
    }
};

const staticTranslations = {
    "shop produce": { en: "Shop Produce", mr: "ताजी खरेदी", hi: "ताज़ा उत्पाद" },
    "vegetables": { en: "Vegetables", mr: "भाज्या", hi: "सब्जियां" },
    "fruits": { en: "Fruits", mr: "फळे", hi: "फल" },
    "browse categories": { en: "Browse categories", mr: "श्रेणीनुसार खरेदी", hi: "श्रेणी ब्राउज़ करें" },
    "sign in": { en: "Sign In", mr: "लॉगिन", hi: "साइन इन" },
    "register": { en: "Register", mr: "नोंदणी", hi: "पंजीकरण" },
    "logout": { en: "Logout", mr: "बाहेर पडा", hi: "लॉग आउट" },
    "fresh from farm": { en: "Fresh From Farm", mr: "थेट शेतातून", hi: "ताज़ा खेत से" },
    "to your doorstep": { en: "To Your Doorstep", mr: "आपल्या दारापर्यंत", hi: "आपके दरवाजे तक" },
    "shop fresh produce": { en: "Shop Fresh Produce", mr: "खरेदी सुरू करा", hi: "उत्पाद खरीदें" },
    "join as farmer": { en: "Join as Farmer", mr: "शेतकरी म्हणून जोडा", hi: "किसान बनें" },
    "100% organic & farm fresh": { en: "100% Organic & Farm Fresh", mr: "१००% सेंद्रिय आणि ताजे", hi: "100% जैविक और ताज़ा" },
    "shopping cart": { en: "Shopping Cart", mr: "माझे कार्ट", hi: "शॉपिंग कार्ट" },
    "your cart is empty.": { en: "Your cart is empty.", mr: "तुमची कार्ट रिकामी आहे.", hi: "आपकी कार्ट खाली है।" },
    "subtotal": { en: "Subtotal", mr: "एकूण रक्कम", hi: "कुल राशि" },
    "proceed to checkout": { en: "Proceed to Checkout", mr: "चेकआउट करा", hi: "चेकआउट करें" },
    "my wishlist": { en: "My Wishlist", mr: "माझी इच्छासूची", hi: "मेरी इच्छासूची" },
    "customer dashboard": { en: "Customer Dashboard", mr: "ग्राहक डॅशबोर्ड", hi: "ग्राहक डैशबोर्ड" },
    "farmer dashboard": { en: "Farmer Dashboard", mr: "शेतकरी डॅशबोर्ड", hi: "किसान डैशबोर्ड" },
    "admin dashboard": { en: "Admin Dashboard", mr: "अ‍ॅडमिन डॅशबोर्ड", hi: "एडमिन डैशबोर्ड" },
    "manage products": { en: "Manage Products", mr: "पिके व्यवस्थापित करा", hi: "उत्पाद प्रबंधन" },
    "order requests": { en: "Order Requests", mr: "ऑर्डर विनंत्या", hi: "ऑर्डर अनुरोध" },
    "total revenue": { en: "Total Revenue", mr: "एकूण महसूल", hi: "कुल राजस्व" },
    "total orders received": { en: "Total Orders Received", mr: "एकूण मिळालेल्या ऑर्डर्स", hi: "कुल प्राप्त ऑर्डर्स" },
    "low stock alert": { en: "Low Stock Alert", mr: "कमी साठा चेतावणी", hi: "कम स्टॉक चेतावनी" },
    "pending order requests": { en: "Pending Order Requests", mr: "प्रलंबित ऑर्डर्स", hi: "लंबित ऑर्डर्स" },
    "email address": { en: "Email Address", mr: "ईमेल पत्ता", hi: "ईमेल पता" },
    "password": { en: "Password", mr: "पासवर्ड", hi: "पासवर्ड" },
    "login": { en: "Login", mr: "लॉगिन करा", hi: "लॉग इन करें" },
    "full name": { en: "Full Name", mr: "पूर्ण नाव", hi: "पूरा नाम" },
    "phone number": { en: "Phone Number", mr: "मोबाईल नंबर", hi: "फ़ोन नंबर" },
    "address": { en: "Address", mr: "पत्ता", hi: "पता" },
    "register as customer": { en: "Register as Customer", mr: "ग्राहक म्हणून नोंदणी करा", hi: "ग्राहक के रूप में पंजीकरण करें" },
    "register as farmer": { en: "Register as Farmer", mr: "शेतकरी म्हणून नोंदणी करा", hi: "किसान के रूप में पंजीकरण करें" }
};

// Change language handler
function changeLanguage(lang) {
    localStorage.setItem("f2c_lang", lang);
    translatePage();
}

// Automatic UI Translation Engine
function translatePage() {
    const lang = localStorage.getItem("f2c_lang") || "en";
    
    // Update language switcher label
    const label = document.getElementById("current-lang-label");
    if (label) {
        if (lang === "mr") label.textContent = "मराठी";
        else if (lang === "hi") label.textContent = "हिन्दी";
        else label.textContent = "English";
    }

    // Dynamic translate elements with data-trn-key
    document.querySelectorAll("[data-trn-key]").forEach(el => {
        const key = el.getAttribute("data-trn-key");
        if (translations[lang] && translations[lang][key]) {
            el.innerHTML = translations[lang][key];
        }
    });

    // Translate inputs with data-trn-placeholder
    document.querySelectorAll("[data-trn-placeholder]").forEach(el => {
        const key = el.getAttribute("data-trn-placeholder");
        if (translations[lang] && translations[lang][key]) {
            el.setAttribute("placeholder", translations[lang][key]);
        }
    });

    // Fallback automatic translation for common strings
    document.querySelectorAll(".nav-link, .nav-item a, h1, h3, h5, h6, label, button:not(#langDropdown), p, span:not(.badge)").forEach(el => {
        const txt = el.textContent.trim().toLowerCase();
        if (staticTranslations[txt]) {
            el.textContent = staticTranslations[txt][lang];
        }
    });
}

// Render dynamic elements on load
document.addEventListener("DOMContentLoaded", () => {
    initTheme();
    updateNavbar();
    translatePage();
});

// Update Navbar links dynamically based on Auth Session
function updateNavbar() {
    const user = getCurrentUser();
    const navAuthContainer = document.getElementById("nav-auth-container");
    if (!navAuthContainer) return;

    const prefix = getRelativePrefix();
    const lang = localStorage.getItem("f2c_lang") || "en";

    // 1. Language selector HTML
    const langSelectorHtml = `
        <div class="dropdown me-2">
            <button class="btn btn-sm btn-outline-success dropdown-toggle rounded-pill px-3" type="button" id="langDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                🌐 <span id="current-lang-label">English</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow border-color" aria-labelledby="langDropdown">
                <li><a class="dropdown-item" href="#" onclick="changeLanguage('en'); event.preventDefault();">English</a></li>
                <li><a class="dropdown-item" href="#" onclick="changeLanguage('mr'); event.preventDefault();">मराठी</a></li>
                <li><a class="dropdown-item" href="#" onclick="changeLanguage('hi'); event.preventDefault();">हिन्दी</a></li>
            </ul>
        </div>
    `;

    if (user) {
        let dashboardPath = prefix + "customer/dashboard.html";
        if (user.role === "FARMER") dashboardPath = prefix + "farmer/dashboard.html";
        if (user.role === "ADMIN") dashboardPath = prefix + "admin/dashboard.html";

        let roleBadge = `<span class="badge bg-success me-2">${user.role}</span>`;
        if (user.role === "ADMIN") roleBadge = `<span class="badge bg-danger me-2">${user.role}</span>`;
        if (user.role === "FARMER") roleBadge = `<span class="badge bg-warning text-dark me-2">${user.role}</span>`;

        let cartLink = "";
        if (user.role === "CUSTOMER") {
            cartLink = `
                <a class="nav-link position-relative me-2" href="${prefix}cart.html">
                    <i class="bi bi-cart3 fs-5"></i>
                    <span id="cart-badge-count" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none">0</span>
                </a>
            `;
        }

        // 2. Notification Dropdown HTML
        const notifDropdownHtml = `
            <div class="dropdown me-2">
                <a class="nav-link position-relative text-dark" href="#" id="notifDropdown" data-bs-toggle="dropdown" aria-expanded="false" onclick="fetchNotificationsDropdown(); event.preventDefault();">
                    <i class="bi bi-bell fs-5"></i>
                    <span id="notif-badge-count" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none">0</span>
                </a>
                <div class="dropdown-menu dropdown-menu-end shadow border-color p-0" aria-labelledby="notifDropdown" style="width: 320px; max-height: 400px; overflow-y: auto;">
                    <div class="p-2 border-bottom fw-bold d-flex justify-content-between align-items-center bg-light">
                        <span id="notif-title-label">Notifications</span>
                        <button class="btn btn-link btn-xs p-0 text-decoration-none small text-success" onclick="markAllNotificationsRead(); event.stopPropagation();">Mark all read</button>
                    </div>
                    <div id="notif-dropdown-list" class="list-group list-group-flush">
                        <div class="text-center py-3 text-muted">No notifications.</div>
                    </div>
                </div>
            </div>
        `;

        navAuthContainer.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                ${langSelectorHtml}
                ${notifDropdownHtml}
                ${cartLink}
                <a class="nav-link me-2" href="${dashboardPath}">
                    <i class="bi bi-person-circle fs-5 me-1"></i> Hello, ${user.name.split(" ")[0]}
                </a>
                ${roleBadge}
                <button onclick="logout()" class="btn btn-outline-danger btn-sm rounded-pill px-3">Logout</button>
            </div>
        `;
        
        if (user.role === "CUSTOMER") {
            fetchCartCount();
        }
        
        // Fetch notifications count
        fetchNotificationsCount();
        
    } else {
        navAuthContainer.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                ${langSelectorHtml}
                <a href="${prefix}login.html" class="btn btn-outline-success rounded-pill px-4">Sign In</a>
                <a href="${prefix}register.html" class="btn btn-success rounded-pill px-4">Register</a>
            </div>
        `;
    }
}

// Fetch Cart Count for Badge
async function fetchCartCount() {
    const user = getCurrentUser();
    if (!user || user.role !== "CUSTOMER") return;
    try {
        const cartItems = await apiFetch("/api/cart");
        const badge = document.getElementById("cart-badge-count");
        if (badge) {
            if (cartItems.length > 0) {
                badge.textContent = cartItems.length;
                badge.classList.remove("d-none");
            } else {
                badge.classList.add("d-none");
            }
        }
    } catch (e) {
        console.error("Error fetching cart count:", e);
    }
}

// Fetch Notifications Count
async function fetchNotificationsCount() {
    const user = getCurrentUser();
    if (!user) return;
    try {
        const unreadCount = await apiFetch("/api/notifications/unread-count");
        const badge = document.getElementById("notif-badge-count");
        if (badge) {
            if (unreadCount > 0) {
                badge.textContent = unreadCount;
                badge.classList.remove("d-none");
            } else {
                badge.classList.add("d-none");
            }
        }
    } catch (e) {
        console.error("Error fetching notification count:", e);
    }
}

// Fetch Notifications List for Dropdown
async function fetchNotificationsDropdown() {
    const listContainer = document.getElementById("notif-dropdown-list");
    if (!listContainer) return;
    
    const lang = localStorage.getItem("f2c_lang") || "en";
    
    try {
        const notifications = await apiFetch("/api/notifications");
        listContainer.innerHTML = "";
        
        if (notifications.length === 0) {
            listContainer.innerHTML = `<div class="text-center py-3 text-muted">No notifications.</div>`;
            return;
        }
        
        notifications.slice(0, 10).forEach(notif => {
            // Priority styling
            let priorityBadge = "";
            if (notif.priority === "HIGH" || notif.priority === "URGENT") {
                priorityBadge = `<span class="badge bg-danger ms-1" style="font-size:9px;">${notif.priority}</span>`;
            }

            // Type styling
            let bgClass = "bg-white";
            if (!notif.isRead) {
                bgClass = "bg-light border-start border-4 border-success";
            }
            
            // Translate notification using template placeholder replacements
            let title = translations[lang][notif.title] || notif.title;
            let message = translations[lang][notif.message] || notif.message;
            if (notif.relatedEntityId) {
                message = message.replace("{orderId}", notif.relatedEntityId)
                                 .replace("{productId}", notif.relatedEntityId)
                                 .replace("{farmerId}", notif.relatedEntityId);
            }
            
            const item = document.createElement("div");
            item.className = `list-group-item list-group-item-action p-2 position-relative ${bgClass}`;
            item.style.cursor = "pointer";
            item.onclick = () => markNotificationRead(notif.id);
            
            item.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <span class="fw-bold">${title}</span>
                    <div class="d-flex gap-1 align-items-center">
                        ${priorityBadge}
                        <button class="btn btn-link btn-xs text-danger p-0 ms-2" onclick="deleteNotification(${notif.id}); event.stopPropagation();">
                            <i class="bi bi-trash3-fill" style="font-size:11px;"></i>
                        </button>
                    </div>
                </div>
                <p class="mb-1 text-muted small">${message}</p>
                <div class="text-end text-black-50" style="font-size: 9px;">
                    ${new Date(notif.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                </div>
            `;
            
            listContainer.appendChild(item);
        });
    } catch (e) {
        listContainer.innerHTML = `<div class="text-center py-3 text-danger">Failed to load notifications.</div>`;
    }
}

// Mark single notification read
async function markNotificationRead(id) {
    try {
        await apiFetch(`/api/notifications/${id}/read`, { method: "PUT" });
        fetchNotificationsCount();
        fetchNotificationsDropdown();
    } catch (err) {
        console.error("Failed to mark read:", err);
    }
}

// Mark all read
async function markAllNotificationsRead() {
    try {
        await apiFetch("/api/notifications/read-all", { method: "PUT" });
        fetchNotificationsCount();
        fetchNotificationsDropdown();
    } catch (err) {
        console.error("Failed to mark all read:", err);
    }
}

// Delete notification
async function deleteNotification(id) {
    try {
        await apiFetch(`/api/notifications/${id}`, { method: "DELETE" });
        fetchNotificationsCount();
        fetchNotificationsDropdown();
    } catch (err) {
        console.error("Failed to delete notification:", err);
    }
}

async function loadReviews(productId) {
    const reviewContainer = document.getElementById("reviews-container");
    try {
        const reviews = await apiFetch(`/api/reviews/product/${productId}`);
        reviewContainer.innerHTML = "";
        if (reviews.length === 0) {
            reviewContainer.innerHTML = "<p>No reviews yet.</p>";
            return;
        }
        reviews.forEach(review => {
            reviewContainer.innerHTML += `
                <div class="card mb-3 p-3">
                    <h6>${review.user?.fullName || review.user?.name || "Customer"}</h6>
                    <p>⭐ ${review.rating}/5</p>
                    <p>${review.comment}</p>
                </div>
            `;
        });
    } catch (error) {
        reviewContainer.innerHTML = "<p class='text-danger'>Failed to load reviews.</p>";
        console.error(error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const productId = new URLSearchParams(window.location.search).get("id");
    if (productId) {
        loadReviews(productId);
    }
});

// Global Quick Add Cart handler
async function quickAddToCart(productId, quantity) {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "login.html";
        return;
    }
    if (user.role !== "CUSTOMER") {
        alert("Only customers can purchase products!");
        return;
    }
    try {
        await apiFetch(`/api/cart/add?productId=${productId}&quantity=${quantity}`, {
            method: "POST"
        });
        fetchCartCount();
        alert("Product added to cart!");
    } catch (e) {
        alert(e.message || "Failed to add item to cart.");
    }
}