package com.github.adrian83.robome.domain.stage.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public record NewStageCommand(String title, UUID userId, TableKey tableKey) {

}
