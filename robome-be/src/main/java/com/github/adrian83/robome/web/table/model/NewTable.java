package com.github.adrian83.robome.web.table.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
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

}
