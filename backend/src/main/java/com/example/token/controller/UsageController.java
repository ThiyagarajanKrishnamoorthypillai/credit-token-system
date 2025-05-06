/*package com.example.token.controller;

import com.example.token.model.User;
import com.example.token.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final UserRepository userRepo;

    public UsageController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/deduct")
public ResponseEntity<?> deductCredits(@RequestBody Map<String, Object> request) {
    // Validate the incoming request for required fields
    String googleId = (String) request.get("googleId");
    if (googleId == null || !request.containsKey("amount")) {
        return ResponseEntity.badRequest().body("Invalid request body");
    }

    int amount = (int) request.get("amount");

    // Proceed with the rest of the logic if validation passes
    Optional<User> optionalUser = userRepo.findByGoogleId(googleId);

    if (optionalUser.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
    }

    User user = optionalUser.get();

    if (user.isBlacklistFlag()) {
        return ResponseEntity.status(403).body("Access denied. User is blacklisted.");
    }

    if (user.getCredits() < amount) {
        return ResponseEntity.status(400).body("Insufficient credits");
    }

    user.setCredits(user.getCredits() - amount);
    user.setUpdatedAt(Instant.now());
    userRepo.save(user);

    return ResponseEntity.ok(Map.of("remainingCredits", user.getCredits()));
}

}

*/

package com.example.token.controller;

import com.example.token.model.User;
import com.example.token.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Date;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    @Autowired 
    private final UserRepository userRepo;

    public UsageController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/deduct")
    public ResponseEntity<?> deductCredit(@RequestBody Map<String, Object> request) {
        String googleId = (String) request.get("googleId");
        if (googleId == null || !request.containsKey("amount")) {
            return ResponseEntity.badRequest().body("Invalid request body");
        }

        int amount = (int) request.get("amount");

        Optional<User> optionalUser = userRepo.findByGoogleId(googleId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get();

        if (user.isBlacklistFlag()) {
            return ResponseEntity.status(403).body("Access denied. User is blacklisted.");
        }

        if (user.getCredit() < amount) {
            return ResponseEntity.status(400).body("Insufficient credits");
        }

        // Deduct credit
        user.setCredit(user.getCredit() - amount);
        user.setUpdatedAt(new Date());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("remainingCredit", user.getCredit()));
    }

    @GetMapping("/user/history")
    public ResponseEntity<?> getImageHistory(@RequestParam String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get();
        return ResponseEntity.ok(Map.of(
                "imageHistory", user.getImageHistory(),
                "credit", user.getCredit()
        ));
    }

    @GetMapping("/credits")
    public ResponseEntity<?> getCredit(@RequestParam String googleId) {
        Optional<User> optionalUser = userRepo.findByGoogleId(googleId);
        if (optionalUser.isEmpty()) {
            // Return ResponseEntity with 404 if the user is not found
            return ResponseEntity.status(404).body("User not found");
        }
    
        User user = optionalUser.get();
        return ResponseEntity.ok(Map.of("credit", user.getCredit()));  // Corrected to return credit as Map
    }
    
}
