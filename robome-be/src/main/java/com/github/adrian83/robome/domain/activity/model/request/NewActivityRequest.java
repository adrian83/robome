package com.github.adrian83.robome.domain.activity.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public record NewActivityRequest(String name, UUID userId, StageKey stageKey) {}
