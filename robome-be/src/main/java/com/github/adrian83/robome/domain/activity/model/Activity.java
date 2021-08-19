package com.github.adrian83.robome.domain.activity.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Activity(
    ActivityKey key,
    UUID userId,
    String name,
    ActivityState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt) {}
