package com.smartcanteen.service;

import com.smartcanteen.entity.MenuItem;
import com.smartcanteen.entity.Stall;
import com.smartcanteen.repository.MenuItemRepository;
import com.smartcanteen.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final StallRepository    stallRepository;

    /** All available items, grouped by category — powers the frontend menu page */
    public Map<String, List<MenuItem>> getMenuGroupedByCategory() {
        return menuItemRepository.findByAvailableTrue()
                .stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
    }

    public List<MenuItem> getAllAvailable() {
        return menuItemRepository.findByAvailableTrue();
    }

    public MenuItem getById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + id));
    }

    /** Admin: add new item */
    public MenuItem save(MenuItem item) {
        return menuItemRepository.save(item);
    }

    /** Admin: toggle availability */
    public MenuItem toggleAvailability(Long id) {
        MenuItem item = getById(id);
        item.setAvailable(!item.isAvailable());
        return menuItemRepository.save(item);
    }

    public void delete(Long id) {
        menuItemRepository.deleteById(id);
    }

    public List<Stall> getAllStalls() {
        return stallRepository.findAll();
    }
}
