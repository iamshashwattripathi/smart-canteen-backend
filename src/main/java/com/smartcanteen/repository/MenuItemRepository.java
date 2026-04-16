package com.smartcanteen.repository;

import com.smartcanteen.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
	List<MenuItem> findByAvailableTrue();

	List<MenuItem> findByCategory(String category);

	List<MenuItem> findByStall_Id(Long stallId);
}
