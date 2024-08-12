package com.github.adrian83.robome.domain.table.model.request;

import java.util.UUID;

public record NewTableCommand(String title, String description, UUID userId) {

}
