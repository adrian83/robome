package com.github.adrian83.robome.domain.stage.model.request;

import com.github.adrian83.robome.domain.stage.model.StageKey;

public record UpdateStageRequest(String title, StageKey stageKey) {
}
