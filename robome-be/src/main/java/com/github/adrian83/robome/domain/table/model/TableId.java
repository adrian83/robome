package com.github.adrian83.robome.domain.table.model;

import java.util.UUID;

public class TableId {

	private UUID tableId;

	public TableId(UUID tableId) {
		super();
		this.tableId = tableId;
	}

	public static TableId fromString(String uuidStr) {
		return new TableId(UUID.fromString(uuidStr));
	}
	
	public UUID getTableId() {
		return tableId;
	}

}
