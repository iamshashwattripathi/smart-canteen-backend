package com.smartcanteen.dto;

import lombok.*;

@Data
@Builder
public class AISuggestionResponse {
	private String recommendedItem;
	private String basis;
}
