package com.github.adrian83.robome.web.staticfiles;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.web.common.PathParams;
import com.github.adrian83.robome.web.common.routes.RouteSupplier;
import com.google.inject.Inject;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class StaticController extends AllDirectives implements PathParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticController.class);

    public static final String CSS_PATH = "/css/{filename}";
    public static final String JS_PATH = "/js/{filename}";

    @Inject
    public StaticController() {
    }

    public Route createRoute() {
        return route(
                get(new RouteSupplier(CSS_PATH, (pathParams) -> serveCssFile(pathParams.get("filename")))),
                get(new RouteSupplier(JS_PATH, (pathParams) -> serveJsFile(pathParams.get("filename"))))
        );
    }

    private Route serveCssFile(String filename) {
        LOGGER.info("Serving CSS file: {}", filename);

        String resourcePath = "/static/css/" + filename;
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            LOGGER.warn("CSS file not found: {}", filename);
            return complete(
                    HttpResponse.create()
                            .withStatus(404)
                            .withEntity(ContentTypes.TEXT_PLAIN_UTF8, "CSS file not found: " + filename)
            );
        }

        String cssContent = new String(readAllBytes(inputStream), StandardCharsets.UTF_8);

        return complete(
                HttpResponse.create()
                        .withStatus(200)
                        .withEntity(MediaTypes.TEXT_CSS.toContentType(HttpCharsets.UTF_8), cssContent)
        );
    }

    private Route serveJsFile(String filename) {
        LOGGER.info("Serving JS file: {}", filename);

        String resourcePath = "/static/js/" + filename;
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            LOGGER.warn("JS file not found: {}", filename);
            return complete(
                    HttpResponse.create()
                            .withStatus(404)
                            .withEntity(ContentTypes.TEXT_PLAIN_UTF8, "JS file not found: " + filename)
            );
        }

        String jsContent = new String(readAllBytes(inputStream), StandardCharsets.UTF_8);

        return complete(
                HttpResponse.create()
                        .withStatus(200)
                        .withEntity(MediaTypes.APPLICATION_JAVASCRIPT.toContentType(HttpCharsets.UTF_8), jsContent)
        );
    }

    private byte[] readAllBytes(InputStream inputStream) {
        try (inputStream) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource", e);
        }
    }
}
