// Cart rendering and Checkout processing script

document.addEventListener("DOMContentLoaded", () => {
    const cartContainer = document.getElementById("cart-items-container");
    if (cartContainer) {
        loadCartItems();
    }

    const checkoutSummary = document.getElementById("checkout-summary");
    if (checkoutSummary) {
        loadCheckoutSummary();
    }

    const checkoutForm = document.getElementById("checkout-form");
    if (checkoutForm) {
        checkoutForm.addEventListener("submit", handleCheckoutSubmit);
    }
});

async function loadCartItems() {
    const container = document.getElementById("cart-items-container");
    const subtotalEl = document.getElementById("cart-subtotal");
    const totalEl = document.getElementById("cart-total");
    if (!container) return;

    container.innerHTML = `
        <tr>
            <td colspan="5" class="text-center py-4">
                <div class="spinner-border text-success" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </td>
        </tr>
    `;

    try {
        const items = await apiFetch("/api/cart");
        if (items.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-5">
                        <i class="bi bi-cart-x fs-1 text-muted d-block mb-3"></i>
                        <h5>Your Cart is Empty</h5>
                        <p class="text-muted">Head to the <a href="/shop.html">Shop</a> to find fresh farm produce.</p>
                    </td>
                </tr>
            `;
            if (subtotalEl) subtotalEl.textContent = "₹0.00";
            if (totalEl) totalEl.textContent = "₹0.00";
            document.getElementById("checkout-btn-link")?.classList.add("disabled");
            return;
        }

        container.innerHTML = "";
        let subtotal = 0;

        items.forEach(item => {
            const p = item.product;
            const itemTotal = p.price * item.quantity;
            subtotal += itemTotal;

            container.innerHTML += `
                <tr>
                    <td class="align-middle">
                        <div class="d-flex align-items-center gap-3">
                            <img src="${p.imageUrl || 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=100'}" 
                                 width="60" height="60" class="rounded object-fit-cover" alt="${p.name}">
                            <div>
                                <h6 class="mb-0 text-truncate" style="max-width: 150px;">${p.name}</h6>
                                <small class="text-muted">By ${p.farmer.name}</small>
                            </div>
                        </div>
                    </td>
                    <td class="align-middle">₹${p.price} / ${p.unit}</td>
                    <td class="align-middle" style="width: 150px;">
                        <div class="input-group input-group-sm">
                            <button class="btn btn-outline-secondary" onclick="updateQty(${item.id}, ${item.quantity - 1})">-</button>
                            <input type="text" class="form-control text-center bg-light" value="${item.quantity}" readonly>
                            <button class="btn btn-outline-secondary" onclick="updateQty(${item.id}, ${item.quantity + 1})">+</button>
                        </div>
                    </td>
                    <td class="align-middle fw-bold text-success">₹${itemTotal.toFixed(2)}</td>
                    <td class="align-middle">
                        <button onclick="removeCartItem(${item.id})" class="btn btn-link text-danger p-0"><i class="bi bi-trash-fill fs-5"></i></button>
                    </td>
                </tr>
            `;
        });

        if (subtotalEl) subtotalEl.textContent = `₹${subtotal.toFixed(2)}`;
        if (totalEl) totalEl.textContent = `₹${subtotal.toFixed(2)}`;
        document.getElementById("checkout-btn-link")?.classList.remove("disabled");

    } catch (e) {
        container.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Failed to load cart items. ${e.message}</td></tr>`;
    }
}

async function updateQty(itemId, newQty) {
    if (newQty <= 0) {
        removeCartItem(itemId);
        return;
    }
    try {
        await apiFetch(`/api/cart/update/${itemId}?quantity=${newQty}`, {
            method: "PUT"
        });
        loadCartItems();
        fetchCartCount();
    } catch (e) {
        alert(e.message || "Failed to update quantity. Maybe product is out of stock.");
    }
}

async function removeCartItem(itemId) {
    if (!confirm("Are you sure you want to remove this item?")) return;
    try {
        await apiFetch(`/api/cart/delete/${itemId}`, {
            method: "DELETE"
        });
        loadCartItems();
        fetchCartCount();
    } catch (e) {
        alert(e.message || "Failed to delete item.");
    }
}

async function loadCheckoutSummary() {
    const summary = document.getElementById("checkout-summary");
    if (!summary) return;

    try {
        const items = await apiFetch("/api/cart");
        let html = '<ul class="list-group list-group-flush mb-3">';
        let total = 0;

        items.forEach(item => {
            const itemTotal = item.product.price * item.quantity;
            total += itemTotal;
            html += `
                <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent px-0 border-color">
                    <div>
                        <span class="fw-semibold">${item.product.name}</span>
                        <small class="text-muted d-block">${item.quantity} ${item.product.unit} @ ₹${item.product.price}</small>
                    </div>
                    <span class="text-success fw-semibold">₹${itemTotal.toFixed(2)}</span>
                </li>
            `;
        });

        html += '</ul>';
        html += `
            <div class="d-flex justify-content-between align-items-center fw-bold fs-5 text-success border-top pt-3 border-color">
                <span>Total Amount:</span>
                <span>₹${total.toFixed(2)}</span>
            </div>
        `;
        summary.innerHTML = html;

    } catch (e) {
        summary.innerHTML = `<div class="alert alert-danger">Failed to load order summary: ${e.message}</div>`;
    }
}

async function handleCheckoutSubmit(e) {
    e.preventDefault();
    const address = document.getElementById("shipping-address").value;
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
    const alertBox = document.getElementById("checkout-alert");
    const submitBtn = document.getElementById("place-order-btn");

    submitBtn.setAttribute("disabled", "disabled");
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Placing Order...';

    try {
        const order = await apiFetch(`/api/orders/checkout?shippingAddress=${encodeURIComponent(address)}&paymentMethod=${paymentMethod}`, {
            method: "POST"
        });
        
        // Show success alerts
        alertBox.className = "alert alert-success";
        alertBox.textContent = `Order placed successfully! Order ID: #${order.id}. Redirecting to tracking screen...`;
        alertBox.classList.remove("d-none");
        
        // Empty cart badge
        fetchCartCount();

        setTimeout(() => {
            window.location.href = `tracking.html?orderId=${order.id}`;
        }, 2500);

    } catch (err) {
        alertBox.className = "alert alert-danger";
        alertBox.textContent = err.message || "Failed to complete checkout.";
        alertBox.classList.remove("d-none");
        submitBtn.removeAttribute("disabled");
        submitBtn.textContent = "Place Order";
    }
}
