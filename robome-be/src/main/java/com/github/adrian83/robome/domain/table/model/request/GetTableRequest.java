package com.github.adrian83.robome.domain.table.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.table.model.TableKey;

public record GetTableRequest(UUID userId, TableKey tableKey) {}
