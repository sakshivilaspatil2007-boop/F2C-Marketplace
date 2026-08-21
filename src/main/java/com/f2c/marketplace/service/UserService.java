package com.f2c.marketplace.service;

import com.f2c.marketplace.config.JwtTokenProvider;
import com.f2c.marketplace.dto.AuthRequest;
import com.f2c.marketplace.dto.AuthResponse;
import com.f2c.marketplace.dto.RegisterRequest;
import com.f2c.marketplace.model.Farmer;
import com.f2c.marketplace.model.Role;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.FarmerRepository;
import com.f2c.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if ("SUSPENDED".equals(user.getStatus())) {
            throw new RuntimeException("Your account is suspended. Please contact admin.");
        }

        // If farmer, check if approved
        if (user.getRole() == Role.FARMER) {
            Farmer farmer = farmerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Farmer profile not found"));
            if ("PENDING".equals(farmer.getVerificationStatus())) {
                throw new RuntimeException("Farmer profile is pending approval by Admin.");
            } else if ("REJECTED".equals(farmer.getVerificationStatus())) {
                throw new RuntimeException("Farmer profile has been rejected by Admin.");
            }
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name(), user.getName(), user.getId());
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getName(), user.getId());
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.FARMER) {
            Farmer farmer = new Farmer();
            farmer.setUser(savedUser);
            farmer.setFarmName(request.getFarmName() != null ? request.getFarmName() : savedUser.getName() + "'s Farm");
            farmer.setFarmAddress(request.getFarmAddress() != null ? request.getFarmAddress() : savedUser.getAddress());
            farmer.setLicenseNumber(request.getLicenseNumber());
            // Farmers start as PENDING approval
            farmer.setVerificationStatus("PENDING");
            farmerRepository.save(farmer);
        }

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }

    public void approveFarmer(Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer profile not found"));
        farmer.setVerificationStatus("APPROVED");
        farmerRepository.save(farmer);
    }

    public void rejectFarmer(Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer profile not found"));
        farmer.setVerificationStatus("REJECTED");
        farmerRepository.save(farmer);
    }
}
