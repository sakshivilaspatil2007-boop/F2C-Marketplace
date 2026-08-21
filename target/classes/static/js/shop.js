// Shop page handler: handles search, filters, and rendering of product grid

let productsList = [];
let selectedCategory = "all";
let maxPrice = 1000;
let minRating = 0;
let searchQuery = "";

document.addEventListener("DOMContentLoaded", async () => {
    // Check if category is passed in URL
    const urlParams = new URLSearchParams(window.location.search);
    const catParam = urlParams.get("category");
    if (catParam) {
        selectedCategory = catParam;
    }
    const queryParam = urlParams.get("search");
    if (queryParam) {
        searchQuery = queryParam;
        const searchInput = document.getElementById("shop-search-input");
        if (searchInput) searchInput.value = queryParam;
    }

    await loadCategories();
    await loadProducts();
    initFilters();
});

async function loadCategories() {
    try {
        const categories = await apiFetch("/api/categories/public/list");
        const filterCategoryList = document.getElementById("filter-category-list");
        if (!filterCategoryList) return;

        filterCategoryList.innerHTML = `
            <button class="list-group-item list-group-item-action ${selectedCategory === 'all' ? 'active' : ''}" 
                    onclick="filterCategory('all', this)">
                All Categories
            </button>
        `;

        categories.forEach(cat => {
            const isActive = selectedCategory == cat.id;
            filterCategoryList.innerHTML += `
                <button class="list-group-item list-group-item-action ${isActive ? 'active' : ''}" 
                        onclick="filterCategory(${cat.id}, this)">
                    ${cat.name}
                </button>
            `;
        });
    } catch (e) {
        console.error("Error loading categories:", e);
    }
}

async function loadProducts() {
    const productGrid = document.getElementById("product-grid");
    if (!productGrid) return;
    
    productGrid.innerHTML = `
        <div class="col-12 text-center py-5">
            <div class="spinner-border text-success" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>
    `;

    try {
        if (searchQuery) {
            productsList = await apiFetch(`/api/products/search?query=${encodeURIComponent(searchQuery)}`);
        } else {
            productsList = await apiFetch("/api/products/public/list");
        }
        applyFilters();
    } catch (e) {
        productGrid.innerHTML = `<div class="alert alert-danger col-12 text-center">Failed to load products. ${e.message}</div>`;
    }
}

function filterCategory(catId, btnElement) {
    selectedCategory = catId;
    
    // Toggle active classes
    const buttons = document.querySelectorAll("#filter-category-list button");
    buttons.forEach(btn => btn.classList.remove("active"));
    btnElement.classList.add("active");

    applyFilters();
}

function initFilters() {
    // Price range slider
    const priceSlider = document.getElementById("price-range-slider");
    const priceLabel = document.getElementById("price-range-val");
    if (priceSlider && priceLabel) {
        priceSlider.addEventListener("input", (e) => {
            maxPrice = parseInt(e.target.value);
            priceLabel.textContent = `₹${maxPrice}`;
            applyFilters();
        });
    }

    // Rating star filter
    const ratingFilter = document.getElementById("rating-filter-select");
    if (ratingFilter) {
        ratingFilter.addEventListener("change", (e) => {
            minRating = parseInt(e.target.value) || 0;
            applyFilters();
        });
    }

    // Sorting
    const sortSelect = document.getElementById("shop-sort-select");
    if (sortSelect) {
        sortSelect.addEventListener("change", () => {
            applyFilters();
        });
    }

    // Search bar submit
    const searchForm = document.getElementById("shop-search-form");
    if (searchForm) {
        searchForm.addEventListener("submit", (e) => {
            e.preventDefault();
            searchQuery = document.getElementById("shop-search-input").value;
            loadProducts();
        });
    }
}

function applyFilters() {
    let filtered = [...productsList];

    // Filter by Category
    if (selectedCategory !== "all") {
        filtered = filtered.filter(p => p.category.id == selectedCategory);
    }

    // Filter by Price
    filtered = filtered.filter(p => p.price <= maxPrice);

    // Apply Sorting
    const sortVal = document.getElementById("shop-sort-select")?.value;
    if (sortVal === "price-low") {
        filtered.sort((a, b) => a.price - b.price);
    } else if (sortVal === "price-high") {
        filtered.sort((a, b) => b.price - a.price);
    } else if (sortVal === "newest") {
        filtered.sort((a, b) => b.id - a.id);
    }

    renderProducts(filtered);
}

function renderProducts(products) {
    const grid = document.getElementById("product-grid");
    if (!grid) return;

    if (products.length === 0) {
        grid.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="bi bi-search fs-1 text-muted d-block mb-3"></i>
                <h5>No Products Found</h5>
                <p class="text-muted">Try adjusting your filters or search terms.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = "";
    products.forEach(p => {
        const ratingHtml = getStarsHtml(4.5); // Default review mock or retrieve from product reviews

        grid.innerHTML += `
            <div class="col-md-4 col-sm-6 mb-4">
                <div class="card h-100">
                    <span class="badge-flash">Fresh</span>
                    <div class="product-img-wrapper">
                        <img src="${p.imageUrl || 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=500'}" 
                             class="product-img" alt="${p.name}">
                    </div>
                    <div class="card-body d-flex flex-column justify-content-between p-3">
                        <div>
                            <span class="text-muted small">${p.category.name}</span>
                            <h5 class="card-title my-1 fs-6 text-truncate">${p.name}</h5>
                            <p class="text-muted small mb-2 text-truncate">${p.description || 'No description available.'}</p>
                            <div class="d-flex align-items-center mb-2">
                                ${ratingHtml} <span class="ms-2 small text-muted">(4.5)</span>
                            </div>
                            <div class="small text-success mb-2">
                                <i class="bi bi-geo-alt-fill me-1"></i> ${p.farmer.address.split(",")[0]} Farm
                            </div>
                        </div>
                        <div>
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <span class="fs-5 fw-bold text-success">₹${p.price} <span class="fs-6 fw-normal text-muted">/ ${p.unit}</span></span>
                                <span class="badge ${p.quantity > 5 ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'} px-2 py-1">
                                    ${p.quantity > 0 ? `${p.quantity} ${p.unit} Left` : 'Out of stock'}
                                </span>
                            </div>
                            <div class="d-flex gap-2">
                                <a href="product.html?id=${p.id}" class="btn btn-outline-success btn-sm w-100 py-2">Details</a>
                                <button onclick="quickAddToCart(${p.id}, 1)" 
                                        class="btn btn-success btn-sm w-100 py-2" ${p.quantity === 0 ? 'disabled' : ''}>
                                    <i class="bi bi-cart-plus me-1"></i> Add
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
}

function getStarsHtml(rating) {
    let stars = "";
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '<i class="bi bi-star-fill text-warning"></i>';
        } else if (i - 0.5 <= rating) {
            stars += '<i class="bi bi-star-half text-warning"></i>';
        } else {
            stars += '<i class="bi bi-star text-muted"></i>';
        }
    }
    return stars;
}


