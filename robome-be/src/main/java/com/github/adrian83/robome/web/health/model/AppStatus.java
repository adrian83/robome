package com.github.adrian83.robome.web.health.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppStatus(@JsonProperty("status") String status) {}
