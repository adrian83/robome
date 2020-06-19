package com.github.adrian83.robome.domain.table;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.TableState;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.model.User;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableServiceTest {

  private TableRepository tableRepositoryMock = Mockito.mock(TableRepository.class);
  private StageService stageServiceMock = Mockito.mock(StageService.class);
  private ActorSystem actorSystem = ActorSystem.create();

  private TableService tableService =
      new TableService(tableRepositoryMock, stageServiceMock, actorSystem);

  private User user = new User("johndoe@somedomain.com", "fbwyflwegrl", newArrayList());

  private TableKey tableKey1 = new TableKey();
  private TableKey tableKey2 = new TableKey();

  private TableEntity tableEntity1 =
      new TableEntity(
          tableKey1, user.getId(), "test_1", "test table 1", TableState.ACTIVE, now(), now());
  private TableEntity tableEntity2 =
      new TableEntity(
          tableKey2, user.getId(), "test_2", "test table 2", TableState.ACTIVE, now(), now());

  private Stage stage = new Stage(tableKey1, user.getId(), "stage");

  @Test
  public void shouldReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stages = newArrayList(stage);
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(stages);

    var tableSource = Source.lazySingle(() -> Optional.of(tableEntity1));

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(User.class), any(TableKey.class))).thenReturn(stagesF);

    // when
    var maybeTableF = tableService.getTable(user, tableKey1);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
    
    // this doesn't for in mockito for now 
    // verify(stageServiceMock).getTableStages(any(User.class), any(TableKey.class));

    var maybeTable = maybeTableF.toCompletableFuture().get(5, SECONDS);
    assertTrue(maybeTable.isPresent());

    var resultTable = maybeTable.get();
    assertTable(tableEntity1, resultTable);

    assertEquals(1, resultTable.getStages().size());
    var resultStage = resultTable.getStages().get(0);

    assertStage(stage, resultStage);
  }

  @Test
  public void shouldNotReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(newArrayList());
    var tableSource = Source.lazySingle(() -> Optional.<TableEntity>empty());

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(User.class), any(TableKey.class))).thenReturn(stagesF);

    // when
    var maybeTableF = tableService.getTable(user, tableKey1);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
    verify(stageServiceMock, never()).getTableStages(any(User.class), any(TableKey.class));

    var maybeTable = maybeTableF.toCompletableFuture().get(2, SECONDS);
    assertFalse(maybeTable.isPresent());
  }

  @Test
  public void shouldReturnUserTables()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var entities = newArrayList(tableEntity1, tableEntity2);
    var tablesSource = Source.from(entities);

    when(tableRepositoryMock.getUserTables(any(UUID.class))).thenReturn(tablesSource);

    // when
    var tablesF = tableService.getTables(user);

    // then
    verify(tableRepositoryMock).getUserTables(any(UUID.class));

    var tables = tablesF.toCompletableFuture().get(2, SECONDS);
    assertEquals(entities.size(), tables.size());

    IntStream.range(0, entities.size()).forEach((i) -> assertTable(entities.get(i), tables.get(i)));
  }

  @Test
  public void shouldSaveTable() throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var newTable = new NewTable("some stuff", "table with my stuff");
    var sink = Sink.fold(Done.done(), (Done d, TableEntity k) -> d);

    when(tableRepositoryMock.saveTable()).thenReturn(sink);

    // when
    var tableF = tableService.saveTable(user, newTable);

    // then
    verify(tableRepositoryMock).saveTable();

    var table = tableF.toCompletableFuture().get(2, SECONDS);
    assertEquals(newTable.getTitle(), table.getTitle());
    assertEquals(newTable.getDescription(), table.getDescription());
  }

  @Test
  public void shouldUpdateTable()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var updatedTable = new UpdatedTable("some stuff", "table with my stuff");
    var sink = Sink.fold(Done.done(), (Done d, TableEntity k) -> d);

    when(tableRepositoryMock.updateTable(any(TableEntity.class))).thenReturn(sink);

    // when
    var tableF = tableService.updateTable(user, tableKey1, updatedTable);

    // then
    verify(tableRepositoryMock).updateTable(any(TableEntity.class));

    var table = tableF.toCompletableFuture().get(2, SECONDS);
    assertEquals(updatedTable.getTitle(), table.getTitle());
    assertEquals(updatedTable.getDescription(), table.getDescription());
  }

  @Test
  public void shouldDeleteTable()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var sink = Sink.fold(Done.done(), (Done d, TableKey k) -> d);

    when(tableRepositoryMock.deleteTable(any(UUID.class))).thenReturn(sink);

    // when
    var tableKeyF = tableService.deleteTable(user, tableKey1);

    // then
    verify(tableRepositoryMock).deleteTable(any(UUID.class));

    var tableKey = tableKeyF.toCompletableFuture().get(2, SECONDS);
    assertEquals(tableKey1, tableKey);
  }

  public void assertTable(TableEntity expected, Table actual) {
    assertEquals(expected.getKey(), actual.getKey());
    assertEquals(expected.getUserId(), actual.getUserId());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getState(), actual.getState());
    assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    assertEquals(expected.getModifiedAt(), actual.getModifiedAt());
  }

  public void assertStage(Stage expected, Stage actual) {
    assertEquals(expected.getKey(), actual.getKey());
    assertEquals(expected.getUserId(), actual.getUserId());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getState(), actual.getState());
    assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    assertEquals(expected.getModifiedAt(), actual.getModifiedAt());
  }
}
