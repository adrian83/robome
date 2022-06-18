package com.github.adrian83.robome.web.common.request;

import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.function.PentaFunction;

import akka.http.javadsl.model.HttpResponse;

@FunctionalInterface
public interface Parameter4Request<P, R, S, T> extends PentaFunction<UserData, P, R, S, T, CompletionStage<HttpResponse>>{}