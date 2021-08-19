package com.github.adrian83.robome.domain.activity.model.request;

import java.util.UUID;

import com.github.adrian83.robome.domain.activity.model.ActivityKey;

public record GetActivityRequest(UUID userId, ActivityKey activityKey) {}
