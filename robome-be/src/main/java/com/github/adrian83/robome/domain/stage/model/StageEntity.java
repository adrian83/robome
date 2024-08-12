package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;

public record StageEntity(StageKey key, String title, StageState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {

}
