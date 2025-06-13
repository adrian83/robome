package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.function.TriFunction;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter1WithBodyRequest<T, BODY>
	extends TriFunction<UserData, T, BODY, CompletionStage<HttpResponse>> {
}