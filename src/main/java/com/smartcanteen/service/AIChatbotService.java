package com.smartcanteen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.entity.ChatMessage;
import com.smartcanteen.entity.MenuItem;
import com.smartcanteen.repository.ChatMessageRepository;
import com.smartcanteen.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatbotService {

    private final ChatMessageRepository chatMessageRepository;
    private final MenuItemRepository    menuItemRepository;
    private final QueueService          queueService;
    private final ObjectMapper          objectMapper;

    @Value("${app.ai.chatbot.api-key}")
    private String apiKey;

    @Value("${app.ai.chatbot.model:gemini-2.0-flash}")
    private String model;

    @Value("${app.ai.chatbot.max-tokens:512}")
    private int maxTokens;

    // ── Send a message and get AI response ──────────────────
    @Transactional
    public String chat(String sessionId, String userMessage, String pageContext) {

        // 1. Save user message
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.USER)
                .content(userMessage)
                .pageContext(pageContext)
                .build());

        // 2. Build conversation history (last 20 messages)
        List<ChatMessage> history =
                chatMessageRepository.findTop20BySessionIdOrderByCreatedAtAsc(sessionId);

        // 3. Call Gemini API
        String reply = callGeminiAPI(history, pageContext);

        // 4. Save assistant reply
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(reply)
                .pageContext(pageContext)
                .build());

        return reply;
    }

    // ── Get chat history for a session ──────────────────────
    public List<ChatMessage> getHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // ── Clear/reset chat session ─────────────────────────────
    @Transactional
    public void clearSession(String sessionId) {
        List<ChatMessage> msgs =
                chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        chatMessageRepository.deleteAll(msgs);
    }

    // ── Call Google Gemini API ───────────────────────────────
    private String callGeminiAPI(List<ChatMessage> history, String pageContext) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_claude_api_key_here")) {
            return "AI chatbot is not configured. Please set app.ai.chatbot.api-key in application.properties.";
        }

        try {
            // Build system instruction text
            String systemText = buildSystemPrompt(pageContext);

            // Build contents array from history
            List<Map<String, Object>> contents = new ArrayList<>();

            for (ChatMessage msg : history) {
                String role = msg.getRole() == ChatMessage.MessageRole.USER ? "user" : "model";
                contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.getContent()))
                ));
            }

            // Request body for Gemini
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("system_instruction", Map.of(
                "parts", List.of(Map.of("text", systemText))
            ));
            requestBody.put("contents", contents);
            requestBody.put("generationConfig", Map.of(
                "maxOutputTokens", maxTokens,
                "temperature", 0.7
            ));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                       + model + ":generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<?, ?> parsed       = objectMapper.readValue(response.body(), Map.class);
                List<?>   candidates   = (List<?>) parsed.get("candidates");
                Map<?, ?>  first       = (Map<?, ?>) candidates.get(0);
                Map<?, ?>  content     = (Map<?, ?>) first.get("content");
                List<?>    parts       = (List<?>) content.get("parts");
                Map<?, ?>  part        = (Map<?, ?>) parts.get(0);
                return part.get("text").toString();
            } else {
                log.error("Gemini API error {}: {}", response.statusCode(), response.body());
                return "I'm having trouble connecting right now. Please try again shortly.";
            }

        } catch (Exception e) {
            log.error("AIChatbotService error: {}", e.getMessage());
            return "Sorry, I couldn't process your request. Please try again.";
        }
    }

    // ── Build context-aware system prompt ───────────────────
    private String buildSystemPrompt(String pageContext) {
        String menuSummary = buildMenuSummary();
        String queueInfo   = buildQueueInfo();

        String pageHelp = switch (pageContext == null ? "general" : pageContext.toLowerCase()) {
            case "home"    -> "The user is on the HOME page. Welcome them and show what they can do.";
            case "menu"    -> "The user is BROWSING THE MENU. Help them choose items, explain prices and veg/non-veg options.";
            case "cart"    -> "The user is at CHECKOUT. Help them review items, choose payment method, and confirm order.";
            case "orders"  -> "The user is viewing ORDER HISTORY. Help them find past orders or track current ones.";
            case "track"   -> "The user is TRACKING AN ORDER. Help them understand order status and pickup token.";
            case "wallet"  -> "The user is on the WALLET PAGE. Explain how to top up and use wallet for payments.";
            case "queue"   -> "The user is viewing QUEUE STATUS. Explain the token system and current wait times.";
            case "profile" -> "The user is on their PROFILE PAGE. Help with account settings.";
            case "admin"   -> "The user is on the ADMIN DASHBOARD. Help with managing orders, inventory, and reports.";
            default        -> "The user needs general help with the canteen app.";
        };

        return """
                You are "Canteen AI", the smart assistant for the Smart Canteen Billing System
                at United College of Engineering & Research (UCER), Prayagraj.

                You help students and staff with:
                - Finding and recommending menu items
                - Explaining the token/queue system
                - Guiding through order placement and checkout
                - Payment options: Cash, UPI, Card, or Wallet
                - Topping up the digital wallet
                - Checking order status (ask for order code like ORD12345)
                - Admin tasks if applicable

                Current page: %s

                Live Menu:
                %s

                Live Queue Status:
                %s

                Guidelines:
                - Be friendly and concise (under 120 words unless detail is needed)
                - Only refer to menu items listed above — never make up items or prices
                - For order tracking, ask for the order code (format: ORDxxxxx)
                - If unsure, suggest speaking to canteen staff
                """.formatted(pageHelp, menuSummary, queueInfo);
    }

    private String buildMenuSummary() {
        try {
            List<MenuItem> items = menuItemRepository.findAll().stream()
                    .filter(MenuItem::isAvailable)
                    .collect(Collectors.toList());

            Map<String, List<String>> grouped = new LinkedHashMap<>();
            for (MenuItem item : items) {
                grouped.computeIfAbsent(item.getCategory(), k -> new ArrayList<>())
                       .add(item.getName() + " ₹" + item.getPrice().toPlainString()
                            + (item.isVeg() ? " [Veg]" : " [Non-Veg]"));
            }

            StringBuilder sb = new StringBuilder();
            grouped.forEach((cat, list) -> {
                sb.append(cat).append(": ");
                sb.append(String.join(", ", list));
                sb.append("\n");
            });
            return sb.toString();
        } catch (Exception e) {
            return "Menu currently unavailable.";
        }
    }

    private String buildQueueInfo() {
        try {
            Map<String, Integer> snapshot = queueService.getQueueSnapshot();
            return snapshot.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue() + " orders waiting")
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Queue info currently unavailable.";
        }
    }
}
