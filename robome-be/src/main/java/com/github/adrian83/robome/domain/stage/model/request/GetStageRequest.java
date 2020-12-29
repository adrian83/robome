package com.github.adrian83.robome.domain.stage.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class GetStageRequest {
	private UUID userId;
	private StageKey stageKey;
}
