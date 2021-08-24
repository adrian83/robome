package com.github.adrian83.robome.domain.stage.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public record UpdateStageRequest(String title, UUID userId, StageKey stageKey) {}
