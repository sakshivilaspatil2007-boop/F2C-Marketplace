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
            const currentPath = window.location.pathname;
            if (currentPath !== "/login.html" && currentPath !== "/register.html" && currentPath !== "/index.html" && currentPath !== "/shop.html") {
                logout();
            }
        }
        
        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || `Request failed with status ${response.status}`);
        }
        
        // Check if response is empty (e.g. DELETE or empty return)
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return await response.json();
        }
        return await response.text();
    } catch (error) {
        console.error("API Call Error:", error);
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

// Render dynamic elements on load
document.addEventListener("DOMContentLoaded", () => {
    initTheme();
    updateNavbar();
});

// Update Navbar links dynamically based on Auth Session
function updateNavbar() {
    const user = getCurrentUser();
    const navAuthContainer = document.getElementById("nav-auth-container");
    if (!navAuthContainer) return;

    const prefix = getRelativePrefix();
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
                <a class="nav-link position-relative" href="${prefix}cart.html">
                    <i class="bi bi-cart3 fs-5"></i>
                    <span id="cart-badge-count" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none">0</span>
                </a>
            `;
        }

        navAuthContainer.innerHTML = `
            <div class="d-flex align-items-center gap-3">
                ${cartLink}
                <a class="nav-link" href="${dashboardPath}">
                    <i class="bi bi-person-circle fs-5 me-1"></i> Hello, ${user.name.split(" ")[0]}
                </a>
                ${roleBadge}
                <button onclick="logout()" class="btn btn-outline-danger btn-sm rounded-pill px-3">Logout</button>
            </div>
        `;
        if (user.role === "CUSTOMER") {
            fetchCartCount();
        }
    } else {
        navAuthContainer.innerHTML = `
            <div class="d-flex align-items-center gap-2">
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
        
        // Update badge count
        fetchCartCount();

        // Visual feedback
        alert("Product added to cart!");
    } catch (e) {
        alert(e.message || "Failed to add item to cart.");
    }
}