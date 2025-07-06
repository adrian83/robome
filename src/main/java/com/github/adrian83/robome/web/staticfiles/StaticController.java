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

    @Inject
    public StaticController() {
    }

    public Route createRoute() {
        return route(
                get(new RouteSupplier(CSS_PATH, (pathParams) -> serveCssFile(pathParams.get("filename"))))
        );
    }

    private Route serveCssFile(String filename) {
        LOGGER.info("Serving CSS file: {}", filename);
        
        try {
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
            
            String cssContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            return complete(
                HttpResponse.create()
                    .withStatus(200)
                    .withEntity(MediaTypes.TEXT_CSS.toContentType(HttpCharsets.UTF_8), cssContent)
            );
            
        } catch (Exception e) {
            LOGGER.error("Error serving CSS file: {}", filename, e);
            return complete(
                HttpResponse.create()
                    .withStatus(500)
                    .withEntity(ContentTypes.TEXT_PLAIN_UTF8, "Error loading CSS file: " + e.getMessage())
            );
        }
    }
}
