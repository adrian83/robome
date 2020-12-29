package com.github.adrian83.robome.domain.stage.model.request;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeleteStageRequest extends GetStageRequest {}
