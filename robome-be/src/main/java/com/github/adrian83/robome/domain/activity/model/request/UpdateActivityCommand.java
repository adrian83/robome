package com.github.adrian83.robome.domain.activity.model.request;

import com.github.adrian83.robome.domain.activity.model.ActivityKey;

public record UpdateActivityCommand(String name, ActivityKey activityKey) {

}
