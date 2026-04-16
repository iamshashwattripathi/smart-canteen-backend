package com.smartcanteen.repository;

import com.smartcanteen.entity.Token;
import com.smartcanteen.entity.Token.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

	@Query("SELECT MAX(t.tokenNumber) FROM Token t WHERE DATE(t.issuedAt) = CURRENT_DATE")
	Optional<Integer> findMaxTokenToday();

	long countByStatus(TokenStatus status);
}
