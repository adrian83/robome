package com.github.adrian83.robome.domain.table.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public record DeleteTableRequest(UUID userId, TableKey tableKey) {}
