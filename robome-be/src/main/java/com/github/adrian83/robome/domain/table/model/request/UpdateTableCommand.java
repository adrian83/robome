package com.github.adrian83.robome.domain.table.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public record UpdateTableCommand(String title, String description, UUID userId, TableKey tableKey) {

}
