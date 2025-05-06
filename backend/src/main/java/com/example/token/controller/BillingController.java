package com.example.token.controller;

import com.example.token.model.User;
import com.example.token.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final UserRepository userRepo;

    public BillingController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/recharge")
    public ResponseEntity<?> recharge(@RequestBody Map<String, Object> body) {
        String googleId = (String) body.get("googleId");
        Integer amount = (Integer) body.get("amount");

        Optional<User> u = userRepo.findByGoogleId(googleId);
        if (u.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = u.get();

        int tokensToAdd = 0;
        if (amount == 50) {
            tokensToAdd = 100;
        } else if (amount == 100) {
            tokensToAdd = 200;
        } else {
            return ResponseEntity.badRequest().body("Unsupported amount");
        }

        user.setCredit(user.getCredit() + tokensToAdd); // ✅ updated to unified credit field
        user.setUpdatedAt(new Date());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("newBalance", user.getCredit()));
    }
}
