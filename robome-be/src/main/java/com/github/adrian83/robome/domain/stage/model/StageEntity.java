package com.github.adrian83.robome.domain.stage.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record StageEntity(
    StageKey key,
    UUID userId,
    String title,
    StageState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt) {}
