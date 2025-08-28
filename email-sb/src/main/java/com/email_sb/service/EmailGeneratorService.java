package com.email_sb.service;

import com.email_sb.controller.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${openrouter.api.url}")
    private String openrouterapiURL;

    @Value("${openrouter.api.key}")
    private String apikey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        // ✅ Use a valid OpenRouter model ID
        Map<String, Object> requestBody = Map.of(
            "model", "meta-llama/llama-3.3-8b-instruct:free",
            "messages", List.of(
                Map.of("role", "system", "content", "You are an assistant that writes professional email replies."),
                Map.of("role", "user", "content", prompt)
            )
        );

        try {
            String response = webClient.post()
                .uri(openrouterapiURL) // e.g. https://openrouter.ai/api/v1/chat/completions
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apikey)
                .header("HTTP-Referer", "http://localhost:8080") // ✅ recommended by OpenRouter
                .header("X-Title", "Email Writer App")           // ✅ optional but good practice
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return extractResponseContent(response);

        } catch (Exception e) {
            return "Error calling OpenRouter API: " + e.getMessage();
        }
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            if (root.has("choices")) {
                return root.path("choices").get(0).path("message").path("content").asText();
            } else if (root.has("error")) {
                return "API Error: " + root.path("error").path("message").asText();
            }
            return "Unexpected API response: " + response;

        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage() + " | Raw response: " + response;
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional reply for the following email content. Please do not generate a subject line. ");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone. ");
        }
        prompt.append("\nOriginal email:\n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
