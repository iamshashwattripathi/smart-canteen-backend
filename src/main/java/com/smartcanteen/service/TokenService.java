package com.smartcanteen.service;

import com.smartcanteen.entity.Token;
import com.smartcanteen.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    /** Generate the next token number for today */
    public Map<String, Object> generateToken() {
        int nextToken = tokenRepository
                .findMaxTokenToday()
                .map(t -> t + 1)
                .orElse(1);

        return Map.of(
                "tokenNumber",  String.format("%03d", nextToken),
                "waiting",      tokenRepository.countByStatus(Token.TokenStatus.WAITING),
                "serving",      tokenRepository.countByStatus(Token.TokenStatus.SERVING)
        );
    }

    /** Mark a token as being served */
    public Token updateStatus(Long tokenId, String status) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        token.setStatus(Token.TokenStatus.valueOf(status.toUpperCase()));
        return tokenRepository.save(token);
    }

    public Map<String, Long> getQueueCounts() {
        return Map.of(
                "waiting", tokenRepository.countByStatus(Token.TokenStatus.WAITING),
                "serving", tokenRepository.countByStatus(Token.TokenStatus.SERVING),
                "done",    tokenRepository.countByStatus(Token.TokenStatus.DONE)
        );
    }
}
