/*package com.example.token.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.token.model.User;
import com.example.token.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/generate")
    public ResponseEntity<?> generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String googleId = request.get("googleId");

        if (prompt == null || prompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt is required");
        }
        if (googleId == null || googleId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google ID is required");
        }

        // 1. Find the user by Google ID
        User user = userRepository.findByGoogleId(googleId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // 2. Check if user has enough credits
        if (user.getCredits() < 40) {
            return ResponseEntity.status(403).body("Insufficient credits");
        }

        try {
            // 3. Send prompt to Flask AI image generator and handle response
            ResponseEntity<Map<String, Object>> response = callFlaskAPI(prompt);
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("No response body from Flask API");
            }
            String imageUrl = (String) body.get("imageUrl");

            // 4. Upload image to Cloudinary using the URL directly
            Map uploadResult = cloudinary.uploader().upload(imageUrl, ObjectUtils.asMap(
                    "resource_type", "image",
                    "public_id", "generated/" + prompt.replaceAll("\\s+", "-") + "-" + System.currentTimeMillis()
            ));

            // 5. Deduct credits and update user
            user.setCredits(user.getCredits() - 40);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);

            // 6. Respond with secure image URL and remaining credits
            return ResponseEntity.ok(Map.of(
                    "imageUrl", uploadResult.get("secure_url"),
                    "remainingCredits", user.getCredits()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Image generation/upload failed: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> callFlaskAPI(String prompt) throws Exception {
        // Example of making the Flask API call and handling its response
        URL url = new URL("http://127.0.0.1:5000/generate-image");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = "{\"prompt\": \"" + prompt + "\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to generate image from Flask API");
        }

        // Read the response from Flask API
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();
        conn.disconnect();

        // Assuming the Flask API returns a JSON response with an 'imageUrl' key
        JSONObject json = new JSONObject(responseBuilder.toString());
        String imageUrl = json.getString("imageUrl");

        // You may return the response body as a map or modify it as needed
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}
*/

package com.example.token.controller;

import com.example.token.model.ImageHistoryEntry;
import com.example.token.model.User;
import com.example.token.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${flask.server.url}")
    private String flaskUrl;

    @PostMapping("/generate")
    public ResponseEntity<?> generateImages(
            @RequestParam String googleId,
            @RequestParam String prompt
    ) {
        Optional<User> userOptional = userRepository.findByGoogleId(googleId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOptional.get();

        if (user.isBlacklistFlag()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are blacklisted.");
        }

        // Use the correct 'credit' field
        if (user.getCredit() < 40) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Insufficient credits");
        }

        try {
            // Prepare Flask request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = Map.of(
                    "prompt", prompt,
                    "userId", googleId
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    flaskUrl + "/generate-image", request, Map.class
            );

            Map<String, Object> flaskData = response.getBody();
            if (flaskData == null || !flaskData.containsKey("images")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Flask server did not return images");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> imagesList = (List<Map<String, Object>>) flaskData.get("images");

            List<String> imageUrls = new ArrayList<>();
            for (Map<String, Object> image : imagesList) {
                if (image.containsKey("imageUrl")) {
                    imageUrls.add(image.get("imageUrl").toString());
                }
            }

            if (imageUrls.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No valid image URLs found");
            }

            // Deduct credits & update timestamp
            user.setCredit(user.getCredit() - 40);
            user.setUpdatedAt(new Date());

            // Save image history for this user
            List<ImageHistoryEntry> history = user.getImageHistory() != null ? user.getImageHistory() : new ArrayList<>();
            for (String url : imageUrls) {
                history.add(new ImageHistoryEntry(url, Date.from(Instant.now())));
            }
            user.setImageHistory(history);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "images", imageUrls,
                    "remainingCredit", user.getCredit()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image generation failed: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImageHistoryEntry>> getImageHistory(@RequestParam String googleId) {
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Only return this user's history
        return ResponseEntity.ok(userOpt.get().getImageHistory());
    }

    @PostMapping("/history/save")
    public ResponseEntity<?> saveImageHistory(@RequestBody Map<String, Object> body) {
        String googleId = (String) body.get("googleId");
        List<String> images = (List<String>) body.get("images");

        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        List<ImageHistoryEntry> history = user.getImageHistory() != null ? user.getImageHistory() : new ArrayList<>();

        for (String url : images) {
            history.add(new ImageHistoryEntry(url, new Date()));
        }

        user.setImageHistory(history);
        user.setUpdatedAt(new Date());
        userRepository.save(user);

        return ResponseEntity.ok("Image history saved.");
    }
}
