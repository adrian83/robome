package com.github.adrian83.robome.domain.stage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatedStage {

  private static final String TITLE_LABEL = "title";

  private String title;

  @JsonCreator
  public UpdatedStage(@JsonProperty(TITLE_LABEL) String title) {
    super();
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
	return "UpdatedStage [title=" + title + "]";
  }

}
