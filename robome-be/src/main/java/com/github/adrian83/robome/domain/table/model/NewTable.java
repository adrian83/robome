package com.github.adrian83.robome.domain.table.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewTable {

  private static final String TITLE_LABEL = "title";
  private static final String DESC_LABEL = "description";

  final String title;
  final String description;

  @JsonCreator
  public NewTable(
      @JsonProperty(TITLE_LABEL) String title, @JsonProperty(DESC_LABEL) String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "NewTable [title=" + title + ", description=" + description + "]";
  }
}
