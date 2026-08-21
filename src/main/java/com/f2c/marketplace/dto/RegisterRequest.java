package com.f2c.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role; // CUSTOMER, FARMER, ADMIN
    private String phone;
    private String address;

    // Farmer-specific fields (optional, only used if role == FARMER)
    private String farmName;
    private String farmAddress;
    private String licenseNumber;
}
