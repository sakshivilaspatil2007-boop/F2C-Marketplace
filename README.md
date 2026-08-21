# F2C – Farmer to Customer Marketplace
### Tagline: *Fresh From Farm To Your Doorstep*

F2C is a modern, enterprise-level Farmer-to-Customer (F2C) e-commerce marketplace built using Java Spring Boot (Backend), MySQL (Database), and a responsive HTML5/CSS3/Bootstrap 5 frontend. 

It replicates experiences from e-commerce giants like Amazon (rich homepage, recommended products, search filters), Flipkart (product grid and category side-panel filtering), and Zomato (real-time order status tracking with coordinates, stepper indicator, and a vector route trace animation on HTML5 Canvas).

---

## 🌟 Key Features

### 1. Customer Panel
* **User Authentication**: Secure signup and login using Spring Security and stateless JWT (JSON Web Tokens).
* **Browsing & Search**: Advanced search bar, filters (price range, categories, ratings), and pagination.
* **Cart & Wishlist**: Interactive cart management with quantity modifications and secure checkout.
* **Live Order Tracking**: Zomato-style order checkpoint stepper showing driver progress (Placed -> Preparing -> Out for Delivery -> Delivered) with a live route canvas animation.
* **Reviews & Ratings**: Add comments and ratings (1-5 Stars) directly on product pages.
* **AI Chatbot**: Floating smart AI assistant that answers queries, makes crop recommendations, and outputs crop predictions.

### 2. Farmer Panel
* **Dashboard Analytics**: Graph charts powered by Chart.js showing inventory levels, monthly sales, and crop pricing trends.
* **Product CRUD**: Add, edit, delete crops/milk products with details (name, description, category, unit, price, and image URLs).
* **Inventory Alerts**: Automatic warning popups when stock levels of any crop fall below critical limits (<= 10 units).
* **Order Management**: Review pending customer order requests, accept/reject, and update status logs.

### 3. Admin Panel
* **Dashboard Analytics**: View total sales revenue, customer count, registered farmer statistics, and system metrics.
* **Farmer Verification**: Review and approve new farmer registrations (verifying license keys) before they can sell items.
* **User Moderation**: Lock/Suspend or Activate customer/farmer accounts.
* **Complaint Box**: Moderates complaint tickets submitted by customers.

---

## 🛠️ Technology Stack
* **Frontend**: HTML5, CSS3 (Vanilla + styling tokens), Bootstrap 5, JavaScript (ES6, Fetch API, LocalStorage).
* **Backend**: Java Spring Boot 3.3.0, Spring Security (JWT), Spring Data JPA, Hibernate, Validation.
* **Database**: MySQL 8.x
* **Build Tool**: Apache Maven (Java 17)

---

## 🚀 Setup & Execution Instructions

### Prerequisites
1. **Java 17 SDK** or higher.
2. **Maven 3.8+** installed and added to your system environment variables.
3. **MySQL Server** running locally.

### Step 1: Create Database
Run the MySQL command prompt or Workbench and execute:
```sql
CREATE DATABASE f2c_db;
```

### Step 2: Configure Properties
Open [application.properties](file:///c:/Users/saksh/OneDrive/Desktop/E-comm/src/main/resources/application.properties) and update the MySQL credentials with your local user and password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/f2c_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3: Run the Application
In your project root directory [E-comm](file:///c:/Users/saksh/OneDrive/Desktop/E-comm), compile and run the Spring Boot server:
```bash
mvn clean compile
mvn spring-boot:run
```
*(Spring Boot will automatically run `data.sql` to populate initial categories, farmers, products, and reviews).*

### Step 4: Access in Browser
Open `http://localhost:8080` in your web browser.

---

## 🔑 Test Credentials (Passwords are `password123` for all)
Use these seeded accounts to test different roles:

| Role | Email | Password | Purpose |
| --- | --- | --- | --- |
| **Admin** | `admin@f2c.com` | `password123` | Moderate users, approve farmers, see system revenue |
| **Farmer 1** | `ramesh@farm.com` | `password123` | Add crops, update status of orders, view sales graphs |
| **Farmer 2** | `suresh@farm.com` | `password123` | Sells cow A2 milk and premium long-grain Basmati Rice |
| **Customer** | `sakshi@customer.com` | `password123` | Search, buy products, track delivery, submit reviews |
