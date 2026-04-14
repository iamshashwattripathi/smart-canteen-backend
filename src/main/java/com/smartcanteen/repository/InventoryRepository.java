package com.smartcanteen.repository;

import com.smartcanteen.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i WHERE i.stockQuantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();
}
