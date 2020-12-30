package com.github.adrian83.robome.domain.activity.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.activity.model.ActivityKey;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class GetActivityRequest {
	private UUID userId;
	private ActivityKey activityKey;
}
