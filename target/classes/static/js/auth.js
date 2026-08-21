// Authentication handler for login.html and register.html

document.addEventListener("DOMContentLoaded", () => {
    // 1. Role Toggle in Register Page
    const roleSelect = document.getElementById("register-role");
    const farmerFields = document.getElementById("farmer-specific-fields");
    
    if (roleSelect && farmerFields) {
        roleSelect.addEventListener("change", () => {
            if (roleSelect.value === "FARMER") {
                farmerFields.classList.remove("d-none");
                // Make inputs required
                document.getElementById("farm-name").setAttribute("required", "required");
                document.getElementById("farm-address").setAttribute("required", "required");
            } else {
                farmerFields.classList.add("d-none");
                document.getElementById("farm-name").removeAttribute("required");
                document.getElementById("farm-address").removeAttribute("required");
            }
        });
    }

    // 2. Handle Login Submission
    const loginForm = document.getElementById("login-form");
    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("login-email").value;
            const password = document.getElementById("login-password").value;
            const alertBox = document.getElementById("auth-alert");

            try {
                const response = await apiFetch("/api/auth/login", {
                    method: "POST",
                    body: JSON.stringify({ email, password })
                });

                // Save token & user
                setAuthToken(response.token);
                setCurrentUser({
                    id: response.id,
                    email: response.email,
                    role: response.role,
                    name: response.name
                });

                // Redirect based on role
                if (response.role === "CUSTOMER") {
                    window.location.href = "index.html";
                } else if (response.role === "FARMER") {
                    window.location.href = "farmer/dashboard.html";
                } else if (response.role === "ADMIN") {
                    window.location.href = "admin/dashboard.html";
                }
            } catch (err) {
                alertBox.textContent = err.message || "Failed to log in. Please check credentials.";
                alertBox.classList.remove("d-none");
            }
        });
    }

    // 3. Handle Registration Submission
    const registerForm = document.getElementById("register-form");
    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            
            const name = document.getElementById("register-name").value;
            const email = document.getElementById("register-email").value;
            const password = document.getElementById("register-password").value;
            const role = document.getElementById("register-role").value;
            const phone = document.getElementById("register-phone").value;
            const address = document.getElementById("register-address").value;
            
            const payload = { name, email, password, role, phone, address };
            
            if (role === "FARMER") {
                payload.farmName = document.getElementById("farm-name").value;
                payload.farmAddress = document.getElementById("farm-address").value;
                payload.licenseNumber = document.getElementById("license-number").value;
            }

            const alertBox = document.getElementById("auth-alert");

            try {
                await apiFetch("/api/auth/register", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });

                // Show success and redirect
                alertBox.className = "alert alert-success";
                alertBox.textContent = "Registration successful! Redirecting to login page...";
                alertBox.classList.remove("d-none");
                setTimeout(() => {
                    window.location.href = "login.html";
                }, 2000);
            } catch (err) {
                alertBox.className = "alert alert-danger";
                alertBox.textContent = err.message || "Failed to register. Please try again.";
                alertBox.classList.remove("d-none");
            }
        });
    }
});
