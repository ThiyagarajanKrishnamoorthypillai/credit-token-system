/* package com.example.token.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
public class FlaskApiService {

    private final String FLASK_API_URL = "http://127.0.0.1:5000/generate-image";  // Flask server URL

    public String generateImage(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("prompt", prompt);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(FLASK_API_URL, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return (String) response.getBody().get("imageUrl");
        } else {
            throw new RuntimeException("Failed to generate image from Flask API");
        }
    }
}
*/