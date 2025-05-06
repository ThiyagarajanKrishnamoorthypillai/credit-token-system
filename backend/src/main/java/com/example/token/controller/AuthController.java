package com.example.token.controller;

import com.example.token.model.User;
import com.example.token.repository.UserRepository;
import com.example.token.service.EmailUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

//import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Forwarded-For", defaultValue = "unknown") String ip
    ) {
        String token = payload.get("token");

        // Verify token with Google
        String googleTokenUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> googleResponse;
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.getForObject(googleTokenUrl, Map.class);
            googleResponse = response;
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Google token");
        }

        if (googleResponse == null || !googleResponse.containsKey("sub") || !googleResponse.containsKey("email")) {
            return ResponseEntity.status(400).body("Invalid Google response");
        }

        String googleId = googleResponse.get("sub");
        String email = googleResponse.get("email");

        // Check if email is disposable
        if (EmailUtils.isDisposable(email)) {
            Optional<User> existingUser = userRepo.findByGoogleId(googleId);
            existingUser.ifPresentOrElse(user -> {
                user.setBlacklistFlag(true);
                user.setUpdatedAt(new Date());
                userRepo.save(user);
            }, () -> {
                User tempUser = new User();
                tempUser.setGoogleId(googleId);
                tempUser.setEmail(email);
                tempUser.setCredit(0);
                tempUser.setIpAddress(ip);
                tempUser.setBlacklistFlag(true);
                tempUser.setCreatedAt(new Date());
                tempUser.setUpdatedAt(new Date());
                userRepo.save(tempUser);
            });
            return ResponseEntity.status(403).body("Temporary emails are not allowed");
        }

        // Create or fetch user
        User user = userRepo.findByGoogleId(googleId).orElseGet(() -> {
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setEmail(email);
            newUser.setCredit(1500);
            newUser.setIpAddress(ip);
            newUser.setBlacklistFlag(false);
            newUser.setCreatedAt(new Date());
            newUser.setUpdatedAt(new Date());
            return userRepo.save(newUser);
        });

        if (user.isBlacklistFlag()) {
            return ResponseEntity.status(403).body("Access denied. IP blacklisted.");
        }

        // Success response
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("credits", user.getCredit());
        response.put("googleId", user.getGoogleId());

        return ResponseEntity.ok(response);
    }
}
