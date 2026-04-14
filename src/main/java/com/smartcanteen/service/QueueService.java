package com.smartcanteen.service;

import com.smartcanteen.entity.Stall;
import com.smartcanteen.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ──────────────────────────────────────────────────────────────
 *  QUEUE SERVICE  —  Core DSA Component
 *
 *  Implements real-time stall load balancing using a Java
 *  PriorityQueue (min-heap).  Each active stall is inserted with
 *  its current queue length as the priority key.  When a new
 *  order arrives, the stall at the top of the heap (smallest
 *  queue length) is selected, its count is incremented, and the
 *  heap is re-ordered — this is the "Queue Optimization" feature
 *  described in the project synopsis.
 *
 *  Time complexity:
 *    assignStall()  →  O(log n)  where n = number of stalls
 *    releaseSlot()  →  O(n log n) to rebuild heap after update
 * ──────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
public class QueueService {

    private final StallRepository stallRepository;

    @Value("${app.queue.avg-prep-time-minutes:3}")
    private int avgPrepTimeMinutes;

    /**
     * Assigns the least-busy stall to a new order using a PriorityQueue (min-heap).
     * This ensures orders are distributed evenly across all counters.
     *
     * @return the Stall with the fewest pending orders
     */
    public Stall assignLeastBusyStall() {
        List<Stall> activeStalls = stallRepository.findByActiveTrue();
        if (activeStalls.isEmpty()) {
            throw new RuntimeException("No active stalls available");
        }

        // Min-heap: stall with smallest currentQueueLength has highest priority
        PriorityQueue<Stall> stallHeap = new PriorityQueue<>(
                Comparator.comparingInt(Stall::getCurrentQueueLength)
        );
        stallHeap.addAll(activeStalls);

        // Poll the least-busy stall (O(log n))
        Stall leastBusy = stallHeap.poll();

        // Increment its queue counter and persist
        assert leastBusy != null;
        leastBusy.setCurrentQueueLength(leastBusy.getCurrentQueueLength() + 1);
        stallRepository.save(leastBusy);

        return leastBusy;
    }

    /**
     * Called when an order is marked COMPLETED or CANCELLED.
     * Decrements the stall's queue counter so the heap stays accurate.
     */
    public void releaseStallSlot(Stall stall) {
        if (stall.getCurrentQueueLength() > 0) {
            stall.setCurrentQueueLength(stall.getCurrentQueueLength() - 1);
            stallRepository.save(stall);
        }
    }

    /**
     * Estimates the pickup time for a new order based on:
     *   pickupTime = now + (queueLength * avgPrepTimeMinutes)
     *
     * @param stall the assigned stall (already incremented)
     * @return estimated pickup LocalDateTime
     */
    public LocalDateTime estimatePickupTime(Stall stall) {
        int waitMins = stall.getCurrentQueueLength() * avgPrepTimeMinutes;
        return LocalDateTime.now().plusMinutes(waitMins);
    }

    /**
     * Returns a snapshot of all stall queue lengths.
     * Used by the admin dashboard to display real-time load.
     */
    public Map<String, Integer> getQueueSnapshot() {
        Map<String, Integer> snapshot = new LinkedHashMap<>();
        stallRepository.findByActiveTrue()
                .forEach(s -> snapshot.put(s.getName(), s.getCurrentQueueLength()));
        return snapshot;
    }
}
