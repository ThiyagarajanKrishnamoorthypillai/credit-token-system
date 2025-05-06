package com.example.token.controller;

import com.example.token.model.User;
import com.example.token.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class CreditController {

    private final UserRepository userRepo;

    public CreditController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // ✅ GET /api/user/credits
    @GetMapping("/user/credits")
    public ResponseEntity<?> getCredits(@RequestParam String googleId) {
        Optional<User> optionalUser = userRepo.findByGoogleId(googleId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        return ResponseEntity.ok(Map.of("credits", optionalUser.get().getCredit()));
    }

    // ✅ POST /api/credits/deduct
    @PostMapping("/credits/deduct")
    public ResponseEntity<?> deductCredits(@RequestBody Map<String, Object> payload) {
        String googleId = (String) payload.get("googleId");
        int cost = (int) payload.get("cost");

        Optional<User> optionalUser = userRepo.findByGoogleId(googleId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = optionalUser.get();
        if (user.getCredit() < cost) {
            return ResponseEntity.status(403).body("Insufficient credits");
        }

        user.setCredit(user.getCredit() - cost);
        user.setUpdatedAt(new Date());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("credits", user.getCredit()));
    }

    // ✅ POST /api/credits/recharge
    @PostMapping("/credits/recharge")
    public ResponseEntity<?> rechargeCredits(@RequestBody Map<String, Object> payload) {
        String googleId = (String) payload.get("googleId");
        int amount = (int) payload.get("amount");

        Optional<User> optionalUser = userRepo.findByGoogleId(googleId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = optionalUser.get();
        user.setCredit(user.getCredit() + amount);
        user.setUpdatedAt(new Date());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("credits", user.getCredit()));
    }
}
