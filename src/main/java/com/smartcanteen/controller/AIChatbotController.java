package com.smartcanteen.controller;

import com.smartcanteen.entity.ChatMessage;
import com.smartcanteen.service.AIChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI Chatbot API (powered by Claude)
 *
 * POST  /api/chat/message          — Send message, receive AI reply
 * GET   /api/chat/history/{sid}    — Get conversation history for a session
 * DELETE /api/chat/session/{sid}   — Clear/reset a chat session
 *
 * All endpoints are public — guests and logged-in users can both chat.
 *
 * Frontend integration:
 *   1. Generate a UUID as sessionId (store in localStorage)
 *   2. POST { sessionId, message, pageContext } → receive { reply, sessionId, timestamp }
 *   3. Display the AI reply in your chatbot widget
 *   4. Pass pageContext from your router (e.g. "menu", "cart", "orders", "track", "wallet")
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AIChatbotController {

    private final AIChatbotService chatbotService;

    /**
     * Send a message to the AI assistant.
     *
     * Request body:
     * {
     *   "sessionId":   "uuid-string",        // unique per user/browser tab
     *   "message":     "What's for lunch?",  // user's message
     *   "pageContext": "menu"                 // current page (home/menu/cart/orders/track/wallet/admin)
     * }
     *
     * Response:
     * {
     *   "reply":     "Here are today's highlights...",
     *   "sessionId": "uuid-string",
     *   "timestamp": "2025-05-01T12:00:00"
     * }
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> message(@RequestBody Map<String, Object> body) {
        String sessionId   = body.getOrDefault("sessionId",   "default").toString();
        String message     = body.getOrDefault("message",     "").toString().trim();
        String pageContext = body.getOrDefault("pageContext",  "general").toString();

        if (message.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "message must not be empty"));
        }

        String reply = chatbotService.chat(sessionId, message, pageContext);

        return ResponseEntity.ok(Map.of(
                "reply",     reply,
                "sessionId", sessionId,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Get full conversation history for a session.
     * Useful to restore chat widget state on page reload.
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> history(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getHistory(sessionId));
    }

    /**
     * Clear/reset chat session (user taps "New Chat" or logs out).
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clear(@PathVariable String sessionId) {
        chatbotService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("status", "Session cleared", "sessionId", sessionId));
    }
}
