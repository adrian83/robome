package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.function.TriFunction;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter2Request<T, P> extends TriFunction<UserData, T, P, CompletionStage<HttpResponse>> {
}
