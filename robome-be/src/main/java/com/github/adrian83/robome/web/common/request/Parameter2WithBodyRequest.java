package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.function.TetraFunction;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter2WithBodyRequest<T, P, BODY> extends TetraFunction<UserData, T, P, BODY, CompletionStage<HttpResponse>> {
}
