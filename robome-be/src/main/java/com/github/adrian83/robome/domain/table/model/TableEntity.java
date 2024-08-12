package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;

public record TableEntity(TableKey key, String title, String description, TableState state, LocalDateTime createdAt, LocalDateTime modifiedAt) {

}
