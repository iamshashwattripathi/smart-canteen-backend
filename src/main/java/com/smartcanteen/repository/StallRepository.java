package com.smartcanteen.repository;

import com.smartcanteen.entity.Stall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StallRepository extends JpaRepository<Stall, Long> {
	List<Stall> findByActiveTrue();
}
