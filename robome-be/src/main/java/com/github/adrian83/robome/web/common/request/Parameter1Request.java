package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.github.adrian83.robome.auth.model.UserData;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter1Request<T> extends BiFunction<UserData, T, CompletionStage<HttpResponse>>{}
