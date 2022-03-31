package com.github.adrian83.robome.domain.activity.model;

import java.time.LocalDateTime;

public record Activity(
    ActivityKey key,
    String name,
    ActivityState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt) {}
