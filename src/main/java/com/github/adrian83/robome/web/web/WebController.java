package com.github.adrian83.robome.web.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.web.common.PathParams;
import com.github.adrian83.robome.web.common.Security;
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
    public static final String CREATE_TABLE = "/create-table";
    public static final String LIST_TABLES = "/tables";

    private static final HttpResponse DEAFULT_VIEW_RENDERING_ERROR_RESP = HttpResponse.create()
            .withStatus(500)
            .withEntity(ContentTypes.TEXT_HTML_UTF8, "<html><body><h1>Error</h1><p>Failed to render page</p></body></html>");

    private final ThymeleafService thymeleafService;
    private final Security security;

    @Inject
    public WebController(ThymeleafService thymeleafService, Security security) {
        this.thymeleafService = thymeleafService;
        this.security = security;
    }

    public Route createRoute() {
        return route(
                get(new RouteSupplier(LOGOUT, (pathParams) -> renderLogoutPage())),
                get(new RouteSupplier(LOGIN, (pathParams) -> renderLoginPage())),
                get(new RouteSupplier(REGISTER, (pathParams) -> renderRegisterPage())),
                get(new RouteSupplier(CREATE_TABLE, (pathParams) -> security.unsecured(pathParams, this::handleCreateTablePage))),
                get(new RouteSupplier(LIST_TABLES, (pathParams) -> security.unsecured(pathParams, this::handleListTablesPage))),
                get(new RouteSupplier(ROOT, (pathParams) -> security.secured(pathParams, this::renderIndexPageForLoggedInUser, this::renderIndexPageForUnloggedUser)))
        );
    }

    private Route renderIndexPageForUnloggedUser() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("isLoggedIn", false);
        variables.put("message", "Welcome");
        variables.put("title", "Welcome");
        return renderPage("index", variables);
    }

    private CompletionStage<HttpResponse> renderIndexPageForLoggedInUser(UserData user, Map<String, String> pathParams) {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("isLoggedIn", true);
        templateVariables.put("message", "Welcome");
        return renderPage2("index", templateVariables);
    }

    private Route renderRegisterPage() {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("message", "Please fill in the form below to create your account.");
        templateVariables.put("title", "Register");
        return renderPage("register", templateVariables);
    }

    private Route renderLoginPage() {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("message", "Please sign in to your account to continue.");
        templateVariables.put("title", "Sign In");
        return renderPage("login", templateVariables);
    }

    private CompletionStage<HttpResponse> handleListTablesPage(Map<String, String> pathParams) {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("title", "My Tables");
        templateVariables.put("message", "Here are all your tables");
        return renderPage2("list-tables", templateVariables);
    }

    private Route renderLogoutPage() {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("message", "You have been logged out successfully.");
        templateVariables.put("title", "Logged Out");
        return renderPage("logout", templateVariables);
    }

    private CompletionStage<HttpResponse> handleCreateTablePage(Map<String, String> pathParams) {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("title", "Create New Table");
        templateVariables.put("message", "Create a new table to organize your work.");
        return renderPage2("create-table", templateVariables);
    }

    private HttpResponse renderHtmlResponse(String templateName, Map<String, Object> variables) {
        String html = thymeleafService.processTemplate(templateName, variables);
        return HttpResponse.create()
                .withStatus(200)
                .withEntity(ContentTypes.TEXT_HTML_UTF8, html);
    }

    private Route renderPage(String templateName, Map<String, Object> variables) {
        try {
            var pageResponse = renderHtmlResponse(templateName, variables);
            return complete(pageResponse);
        } catch (Exception e) {
            LOGGER.error("Error rendering {} page", templateName, e);
            return complete(DEAFULT_VIEW_RENDERING_ERROR_RESP);
        }
    }

    private CompletionStage<HttpResponse> renderPage2(String templateName, Map<String, Object> variables) {
        try {
            var pageResponse = renderHtmlResponse(templateName, variables);
            return CompletableFuture.completedFuture(pageResponse);
        } catch (Exception e) {
            LOGGER.error("Error rendering create table page", e);
            return CompletableFuture.completedFuture(DEAFULT_VIEW_RENDERING_ERROR_RESP);
        }
    }

}
