package com.smartcanteen.service;

import com.smartcanteen.entity.Inventory;
import com.smartcanteen.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    /** Add or update a raw material stock entry */
    public Inventory saveOrUpdate(String materialName, int quantity, String unit, int threshold) {
        // Check if material already exists
        return inventoryRepository.findAll().stream()
                .filter(i -> i.getMaterialName().equalsIgnoreCase(materialName))
                .findFirst()
                .map(existing -> {
                    existing.setStockQuantity(quantity);
                    if (unit     != null) existing.setUnit(unit);
                    if (threshold > 0)   existing.setLowStockThreshold(threshold);
                    return inventoryRepository.save(existing);
                })
                .orElseGet(() -> inventoryRepository.save(
                        Inventory.builder()
                                .materialName(materialName)
                                .stockQuantity(quantity)
                                .unit(unit != null ? unit : "units")
                                .lowStockThreshold(threshold > 0 ? threshold : 5)
                                .build()
                ));
    }

    public void delete(Long id) {
        inventoryRepository.deleteById(id);
    }
}
