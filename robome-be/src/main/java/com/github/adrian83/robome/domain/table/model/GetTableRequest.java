package com.github.adrian83.robome.domain.table.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTableRequest {
	private UUID userId;
	private TableKey tableKey;
	
}
