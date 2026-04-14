package com.smartcanteen.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "stalls")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Stall {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 150)
    private String location;

    @Column(name = "is_active")
    private boolean active = true;

    /**
     * Tracks current queue depth for PriorityQueue-based load balancing.
     * The QueueService picks the stall with the smallest currentQueueLength.
     */
    @Column(name = "current_queue_length")
    private int currentQueueLength = 0;
}
