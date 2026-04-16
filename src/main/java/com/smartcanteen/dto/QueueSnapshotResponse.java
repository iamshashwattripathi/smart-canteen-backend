package com.smartcanteen.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
public class QueueSnapshotResponse {
	private Map<String, Integer> stallQueues;
	private int totalWaiting;
}
