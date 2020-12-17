package com.github.adrian83.robome.domain.stage.model;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListTableStagesRequest {
	private UUID userId;
	private TableKey tableKey;
	
}