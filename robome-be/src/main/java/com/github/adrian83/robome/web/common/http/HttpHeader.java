package com.github.adrian83.robome.web.common.http;

public enum HttpHeader {
    CONTENT_TYPE("Content-Type"), AUTHORIZATION("Authorization"), LOCATION("Location"),
    EXPOSE_HEADERS("access-control-expose-headers");

    private final String text;

    HttpHeader(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
