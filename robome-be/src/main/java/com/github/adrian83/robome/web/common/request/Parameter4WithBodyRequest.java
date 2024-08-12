package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.function.HexaFunction;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter4WithBodyRequest<P, R, S, T, BODY> extends HexaFunction<UserData, P, R, S, T, BODY, CompletionStage<HttpResponse>> {
}
