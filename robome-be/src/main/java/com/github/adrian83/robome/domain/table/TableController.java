package com.github.adrian83.robome.domain.table;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.Authorization;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.common.UserAndForm;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class TableController extends AbstractController {

  public static final String TABLES = "tables";

  private TableService tableService;

  @Inject
  public TableController(
      TableService tableService,
      JwtAuthorizer jwtAuthorizer,
      Config config,
      ExceptionHandler exceptionHandler,
      Response response) {
    super(jwtAuthorizer, exceptionHandler, config, response);
    this.tableService = tableService;
  }

  public Route createRoute() {
    return route(
        createTableOptionsRoute,
        updateTableOptionsRoute,
        getTablesRoute,
        getTableRoute,
        deleteTableRoute,
        updateTableRoute,
        createTableRoute);
  }

  private Route createTableOptionsRoute =
      options(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(this::handleCreateTableOptions)));

  private Route updateTableOptionsRoute =
      options(
          () ->
              pathPrefix(
                  TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId -> pathEndOrSingleSlash(this::handleUpdateTableOptions))));

  private Route getTablesRoute =
      get(() -> pathPrefix(TABLES, () -> pathEndOrSingleSlash(() -> jwtSecured(this::getTables))));

  private Route getTableRoute =
      get(
          () ->
              pathPrefix(
                  TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId ->
                              pathEndOrSingleSlash(
                                  () -> jwtSecured(tableId, this::getTableById)))));

  private Route deleteTableRoute =
      delete(
          () ->
              pathPrefix(
                  TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId ->
                              pathEndOrSingleSlash(() -> jwtSecured(tableId, this::deleteTable)))));

  private Route updateTableRoute =
      put(
          () ->
              pathPrefix(
                  TABLES,
                  () ->
                      pathPrefix(
                          segment(),
                          tableId ->
                              pathEndOrSingleSlash(
                                  () ->
                                      jwtSecured(
                                          tableId, UpdatedTable.class, this::updateTable)))));

  private Route createTableRoute =
      post(
          () ->
              path(
                  TABLES,
                  () ->
                      pathEndOrSingleSlash(() -> jwtSecured(NewTable.class, this::persistTable))));

  private Route getTables(CompletionStage<Optional<User>> maybeUserF) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadTables)
            .thenCompose(tableService::getTables)
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route getTableById(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canReadTables)
            .thenCompose(user -> tableService.getTable(user, TableKey.fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromOptional)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route deleteTable(CompletionStage<Optional<User>> maybeUserF, String tableIdStr) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteTables)
            .thenCompose(user -> tableService.deleteTable(user, TableKey.fromString(tableIdStr)))
            .thenApply(responseProducer::jsonFromObject)
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route updateTable(
      CompletionStage<Optional<User>> maybeUserF, String tableIdStr, UpdatedTable updatedTable) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteTables)
            .thenApply(user -> new UserAndForm<UpdatedTable>(user, updatedTable))
            .thenApply(UserAndForm::validate)
            .thenCompose(
                uaf ->
                    tableService.updateTable(
                        uaf.getUser(), TableKey.fromString(tableIdStr), uaf.getForm()))
            .thenApply(
                table ->
                    responseProducer.response200(
                        location(table.getKey())))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route persistTable(CompletionStage<Optional<User>> maybeUserF, NewTable newTable) {

    CompletionStage<HttpResponse> responseF =
        maybeUserF
            .thenApply(Authentication::userExists)
            .thenApply(Authorization::canWriteTables)
            .thenApply(user -> new UserAndForm<NewTable>(user, newTable))
            .thenApply(UserAndForm::validate)
            .thenCompose(uaf -> tableService.saveTable(uaf.getUser(), uaf.getForm()))
            .thenApply(table -> responseProducer.response201(location(table.getKey())))
            .exceptionally(exceptionHandler::handleException);

    return completeWithFuture(responseF);
  }

  private Route handleCreateTableOptions() {
	  return complete(responseProducer.response200());
  }

  private Route handleUpdateTableOptions() {
    return complete(responseProducer.response200());
  }

  private Location location(TableKey tableKey) {
    return locationFor(TableController.TABLES, tableKey.getTableId().toString());
  }
}
