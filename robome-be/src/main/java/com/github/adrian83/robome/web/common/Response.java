package com.github.adrian83.robome.web.common;

import static com.github.adrian83.robome.web.common.http.HttpHeader.AUTHORIZATION;
import static com.github.adrian83.robome.web.common.http.HttpHeader.CONTENT_TYPE;
import static com.github.adrian83.robome.web.common.http.HttpHeader.LOCATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.web.common.http.Cors;
import com.github.adrian83.robome.web.common.http.HttpMethod;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;

public class Response {

    protected static final String CORS_ORIGIN_KEY = "cors.origin";

    private Config config;
    private ObjectMapper objectMapper;

    @Inject
    public Response(Config config, ObjectMapper objectMapper) {
        super();
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public <T> HttpResponse jsonFromOptional(Optional<T> maybe) {
        return maybe.map(this::jsonFromObject).orElse(response404());
    }

    public HttpResponse jsonFromObject(Object obj) {
        return HttpResponse.create().withStatus(StatusCodes.OK)
                .withEntity(ContentTypes.APPLICATION_JSON, toJsonString(obj)).addHeaders(corsHeaders());
    }

    public HttpResponse response400(List<ValidationError> validationErrors) {
        return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST)
                .withEntity(ContentTypes.APPLICATION_JSON, toJsonString(validationErrors)).addHeaders(corsHeaders());
    }

    public HttpResponse response404() {
        return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).addHeaders(corsHeaders());
    }

    public HttpResponse response200() {
        return HttpResponse.create().withStatus(StatusCodes.OK).addHeaders(corsHeaders());
    }

    public HttpResponse response200(HttpHeader... hdrs) {
        return HttpResponse.create().withStatus(StatusCodes.OK)
                .addHeaders(concantenateHeaders(headers(hdrs), corsHeaders()));
    }

    public HttpResponse response200(HttpMethod... methods) {
        return HttpResponse.create().withStatus(StatusCodes.OK)
                .addHeaders(concantenateHeaders(Lists.newArrayList(Cors.methods(methods)), corsHeaders()));
    }

    public HttpResponse response201(HttpHeader... hdrs) {
        return HttpResponse.create().withStatus(StatusCodes.CREATED)
                .addHeaders(concantenateHeaders(headers(hdrs), corsHeaders()));
    }

    public HttpResponse response401(HttpHeader... hdrs) {
        return HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED).addHeaders(corsHeaders());
    }

    public HttpResponse response500(String msg) {
        return HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR)
                .withEntity(ContentTypes.TEXT_PLAIN_UTF8, toJsonString(msg)).addHeaders(corsHeaders());
    }

    private List<HttpHeader> concantenateHeaders(List<HttpHeader> list1, List<HttpHeader> list2) {
        var result = new ArrayList<HttpHeader>(list1);
        result.addAll(list2);
        return result;
    }

    protected List<HttpHeader> corsHeaders() {
        return headers(Cors.allowHeaders(AUTHORIZATION, CONTENT_TYPE, LOCATION), Cors.origin(corsOrigin()),
                Cors.methods(HttpMethod.ALL), Cors.exposeHeaders(AUTHORIZATION, CONTENT_TYPE, LOCATION));
    }

    protected String corsOrigin() {
        return config.getString(CORS_ORIGIN_KEY);
    }

    protected List<HttpHeader> headers(HttpHeader... headers) {
        return Arrays.asList(headers);
    }

    protected String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
