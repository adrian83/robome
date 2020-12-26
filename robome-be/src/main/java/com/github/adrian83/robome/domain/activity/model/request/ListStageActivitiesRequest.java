package com.github.adrian83.robome.domain.activity.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListStageActivitiesRequest {
	private UUID userId;
	private StageKey stageKey;
}
