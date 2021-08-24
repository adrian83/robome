package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record TableEntity(
    TableKey key,
    UUID userId,
    String title,
    String description,
    TableState state,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt) {}
