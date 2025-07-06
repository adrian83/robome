package com.github.adrian83.robome.web.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.web.common.PathParams;
import com.github.adrian83.robome.web.common.routes.RouteSupplier;
import com.github.adrian83.robome.web.template.ThymeleafService;
import com.google.inject.Inject;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class WebController extends AllDirectives implements PathParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);

    public static final String ROOT = "/";
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
    public static final String LOGOUT = "/logout";

    private final ThymeleafService thymeleafService;

    @Inject
    public WebController(ThymeleafService thymeleafService) {
        this.thymeleafService = thymeleafService;
    }

    public Route createRoute() {
        return route(
                get(new RouteSupplier(LOGOUT, (pathParams) -> renderLogoutPage())),
                get(new RouteSupplier(LOGIN, (pathParams) -> renderLoginPage())),
                get(new RouteSupplier(REGISTER, (pathParams) -> renderRegisterPage())),
                get(new RouteSupplier(ROOT, (pathParams) -> renderIndexPage()))
        );
    }

    private Route renderIndexPage() {
        LOGGER.info("Rendering index page");
        
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("title", "Welcome");

        try {
            String html = thymeleafService.processTemplate("index", templateVariables);
            
            return complete(
                HttpResponse.create()
                    .withStatus(200)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, html)
            );
        } catch (Exception e) {
            LOGGER.error("Error rendering index page", e);
            return complete(
                HttpResponse.create()
                    .withStatus(500)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, 
                        "<html><body><h1>Error</h1><p>Failed to render page: " + e.getMessage() + "</p></body></html>")
            );
        }
    }

    private Route renderRegisterPage() {
        LOGGER.info("Rendering register page");
        
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("title", "Create Account");
        templateVariables.put("message", "Please fill in the form below to create your account.");

        try {
            String html = thymeleafService.processTemplate("register", templateVariables);
            
            return complete(
                HttpResponse.create()
                    .withStatus(200)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, html)
            );
        } catch (Exception e) {
            LOGGER.error("Error rendering register page", e);
            return complete(
                HttpResponse.create()
                    .withStatus(500)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, 
                        "<html><body><h1>Error</h1><p>Failed to render page: " + e.getMessage() + "</p></body></html>")
            );
        }
    }

    private Route renderLoginPage() {
        LOGGER.info("Rendering login page");
        
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("title", "Sign In");
        templateVariables.put("message", "Please sign in to your account to continue.");

        try {
            String html = thymeleafService.processTemplate("login", templateVariables);
            
            return complete(
                HttpResponse.create()
                    .withStatus(200)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, html)
            );
        } catch (Exception e) {
            LOGGER.error("Error rendering login page", e);
            return complete(
                HttpResponse.create()
                    .withStatus(500)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, 
                        "<html><body><h1>Error</h1><p>Failed to render page: " + e.getMessage() + "</p></body></html>")
            );
        }
    }

    private Route renderLogoutPage() {
        LOGGER.info("Rendering logout page");
        
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("title", "Logged Out");
        templateVariables.put("message", "You have been successfully logged out of your account.");

        try {
            String html = thymeleafService.processTemplate("logout", templateVariables);
            
            return complete(
                HttpResponse.create()
                    .withStatus(200)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, html)
            );
        } catch (Exception e) {
            LOGGER.error("Error rendering logout page", e);
            return complete(
                HttpResponse.create()
                    .withStatus(500)
                    .withEntity(ContentTypes.TEXT_HTML_UTF8, 
                        "<html><body><h1>Error</h1><p>Failed to render page: " + e.getMessage() + "</p></body></html>")
            );
        }
    }
}
