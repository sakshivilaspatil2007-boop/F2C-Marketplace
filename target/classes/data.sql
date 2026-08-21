-- Seed Data for F2C - Farmer to Customer Marketplace

-- 1. Insert Users (Password is 'password123' for all, hashed with BCrypt)
-- Hash: $2a$10$Rz29KxG.0oBms2/4YF4NGu24/a7tM6pZ/1P5Z1e5058wS7c9kK3C2
INSERT INTO users (id, name, email, password, role, phone, address, status) VALUES
(1, 'System Admin', 'admin@f2c.com', '$2a$10$Rz29KxG.0oBms2/4YF4NGu24/a7tM6pZ/1P5Z1e5058wS7c9kK3C2', 'ADMIN', '+919999999999', 'Admin HQ, New Delhi', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, name, email, password, role, phone, address, status) VALUES
(2, 'Ramesh Kumar', 'ramesh@farm.com', '$2a$10$Rz29KxG.0oBms2/4YF4NGu24/a7tM6pZ/1P5Z1e5058wS7c9kK3C2', 'FARMER', '+919876543210', 'Green Fields Farm, Sonipat, Haryana', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, name, email, password, role, phone, address, status) VALUES
(3, 'Suresh Patel', 'suresh@farm.com', '$2a$10$Rz29KxG.0oBms2/4YF4NGu24/a7tM6pZ/1P5Z1e5058wS7c9kK3C2', 'FARMER', '+919876543211', 'Saraswati Valley Farm, Anand, Gujarat', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, name, email, password, role, phone, address, status) VALUES
(4, 'Sakshi Sharma', 'sakshi@customer.com', '$2a$10$Rz29KxG.0oBms2/4YF4NGu24/a7tM6pZ/1P5Z1e5058wS7c9kK3C2', 'CUSTOMER', '+919812345678', 'Apt 4B, Green Glen Layout, Bangalore, Karnataka', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (id, name, email, password, role, phone, address, status) VALUES
(5, 'Anjali Verma', 'anjali@customer.com', '$2a$10$Rz29KxG.0oBms2/4YF4NGu24/a7tM6pZ/1P5Z1e5058wS7c9kK3C2', 'CUSTOMER', '+919812345679', 'H-12, Sector 15, Noida, Uttar Pradesh', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- 2. Insert Farmer details
INSERT INTO farmers (id, user_id, farm_name, farm_address, verification_status, license_number, revenue) VALUES
(1, 2, 'Green Fields Farm', 'Sonipat, Haryana', 'APPROVED', 'LIC-FARM-98765', 45000.00)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO farmers (id, user_id, farm_name, farm_address, verification_status, license_number, revenue) VALUES
(2, 3, 'Saraswati Valley Farm', 'Anand, Gujarat', 'APPROVED', 'LIC-FARM-12345', 28000.00)
ON DUPLICATE KEY UPDATE id=id;

-- 3. Insert Categories
INSERT INTO categories (id, name, description, image_url) VALUES
(1, 'Vegetables', 'Fresh and organic vegetables direct from farms', 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=500&auto=format&fit=crop&q=60'),
(2, 'Fruits', 'Sweet, juicy, and naturally ripened farm fruits', 'https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=500&auto=format&fit=crop&q=60'),
(3, 'Grains & Pulses', 'High-quality grains, wheat, rice, and healthy pulses', 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=500&auto=format&fit=crop&q=60'),
(4, 'Dairy & Eggs', 'Pure cow milk, organic eggs, cheese, and butter', 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500&auto=format&fit=crop&q=60'),
(5, 'Herbs & Spices', 'Aromatic spices and fresh medicinal green herbs', 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500&auto=format&fit=crop&q=60')
ON DUPLICATE KEY UPDATE id=id;

-- 4. Insert Products
-- Farmer 1 (Ramesh, user_id 2) products
INSERT INTO products (id, farmer_id, category_id, name, description, price, quantity, unit, image_url, status) VALUES
(1, 2, 1, 'Organic Tomatoes', 'Fresh and juicy red tomatoes grown with natural compost. Ideal for salads and gravies.', 40.00, 150.0, 'kg', 'https://images.unsplash.com/photo-1595855759920-86582396756a?w=500&auto=format&fit=crop&q=60', 'ACTIVE'),
(2, 2, 1, 'Farm Fresh Potatoes', 'High-quality gold potatoes, freshly dug out from fields. Excellent storage life.', 25.00, 300.0, 'kg', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&auto=format&fit=crop&q=60', 'ACTIVE'),
(3, 2, 2, 'Fresh Green Apples', 'Crisp, sweet, and tangy green apples from our orchards. Packed with vitamins.', 120.00, 80.0, 'kg', 'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=500&auto=format&fit=crop&q=60', 'ACTIVE'),
(4, 2, 5, 'Organic Mint Leaves', 'Strongly aromatic fresh mint leaves, harvested on the day of delivery.', 15.00, 50.0, 'bundle', 'https://images.unsplash.com/photo-1618220179428-22790b461013?w=500&auto=format&fit=crop&q=60', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- Farmer 2 (Suresh, user_id 3) products
INSERT INTO products (id, farmer_id, category_id, name, description, price, quantity, unit, image_url, status) VALUES
(5, 3, 4, 'Pure Cow A2 Milk', 'Unprocessed raw A2 cow milk, pasteurized and packed in glass bottles. Rich in nutrients.', 75.00, 100.0, 'litre', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&auto=format&fit=crop&q=60', 'ACTIVE'),
(6, 3, 3, 'Basmati Rice (Premium)', 'Long grain, highly aromatic Basmati Rice aged for 12 months for supreme quality.', 95.00, 500.0, 'kg', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&auto=format&fit=crop&q=60', 'ACTIVE'),
(7, 3, 2, 'Organic Bananas', 'Sweet and naturally ripened farm fresh bananas. High in potassium.', 50.00, 120.0, 'dozen', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&auto=format&fit=crop&q=60', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- 5. Insert Reviews
INSERT INTO reviews (id, user_id, product_id, rating, comment) VALUES
(1, 4, 1, 5, 'Extremely fresh and tasty. Safe to consume raw in salads!'),
(2, 4, 5, 5, 'Best milk I have had in Bangalore. Reminds me of my hometown.'),
(3, 5, 2, 4, 'Good quality potatoes, size is uniform. Recommended.')
ON DUPLICATE KEY UPDATE id=id;
