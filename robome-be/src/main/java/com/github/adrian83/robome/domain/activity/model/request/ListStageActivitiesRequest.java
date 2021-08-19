package com.github.adrian83.robome.domain.activity.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public record ListStageActivitiesRequest(UUID userId, StageKey stageKey) {}
