package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.List;

import com.github.adrian83.robome.domain.stage.model.Stage;

public record Table(TableKey key, String title, String description, TableState state, LocalDateTime createdAt,
	LocalDateTime modifiedAt, List<Stage> stages) {

    public Table withStages(List<Stage> stages) {
	return new Table(key, title, description, state, createdAt, modifiedAt, stages);
    }
}
