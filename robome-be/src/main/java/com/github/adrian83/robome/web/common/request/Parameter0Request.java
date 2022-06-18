package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.github.adrian83.robome.auth.model.UserData;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter0Request extends Function<UserData, CompletionStage<HttpResponse>>{}
