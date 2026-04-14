package com.smartcanteen.repository;

import com.smartcanteen.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    // Keep only the last N messages per session to avoid unbounded context
    List<ChatMessage> findTop20BySessionIdOrderByCreatedAtAsc(String sessionId);
}
