package com.github.adrian83.robome.domain.table;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.StageState;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesRequest;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.TableState;
import com.github.adrian83.robome.domain.table.model.request.GetTableRequest;
import com.github.adrian83.robome.domain.table.model.request.ListTablesRequest;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;

public class TableServiceTest {

  private TableRepository tableRepositoryMock = Mockito.mock(TableRepository.class);
  private StageService stageServiceMock = Mockito.mock(StageService.class);
  private ActorSystem actorSystem = ActorSystem.create();

  private TableService tableService =
      new TableService(tableRepositoryMock, stageServiceMock, actorSystem);

  private UserData user =
		  UserData.builder()
          .id(UUID.randomUUID())
          .email("johndoe@somedomain.com")
          .roles(Collections.emptySet())
          .build();

  private TableKey tableKey1 = TableKey.random();
  private TableKey tableKey2 = TableKey.random();

  private TableEntity tableEntity1 =
      TableEntity.builder()
          .key(tableKey1)
          .userId(user.getId())
          .title("test 1")
          .description("test table 1")
          .state(TableState.ACTIVE)
          .modifiedAt(Time.utcNow())
          .createdAt(Time.utcNow())
          .build();

  private TableEntity tableEntity2 =
      TableEntity.builder()
          .key(tableKey2)
          .userId(user.getId())
          .title("test 2")
          .description("test table 2")
          .state(TableState.ACTIVE)
          .modifiedAt(Time.utcNow())
          .createdAt(Time.utcNow())
          .build();

  private Stage stage =
      Stage.builder()
          .key(StageKey.randomWithTableKey(tableKey1))
          .userId(user.getId())
          .title("stage 1")
          .state(StageState.ACTIVE)
          .modifiedAt(Time.utcNow())
          .createdAt(Time.utcNow())
          .activities(Collections.emptyList())
          .build(); // user.getId(), "stage");

  @Test
  public void shouldReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stages = newArrayList(stage);
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(stages);
    var getTableReq = GetTableRequest.builder().userId(user.getId()).tableKey(tableKey1).build();

    var tableSource = Source.lazySingle(() -> tableEntity1);

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(ListTableStagesRequest.class))).thenReturn(stagesF);

    // when
    var tableF = tableService.getTable(getTableReq);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));

    // this doesn't for in mockito for now
    // verify(stageServiceMock).getTableStages(any(User.class), any(TableKey.class));

    var resultTable = tableF.toCompletableFuture().get(5, SECONDS);

    assertTable(tableEntity1, resultTable);

    assertEquals(1, resultTable.getStages().size());
    var resultStage = resultTable.getStages().get(0);

    assertStage(stage, resultStage);
  }

  @Test()
  public void shouldNotReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(newArrayList());
    Source<TableEntity, NotUsed> tableSource = Source.empty();
    var getTableReq = GetTableRequest.builder().userId(user.getId()).tableKey(tableKey1).build();

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(ListTableStagesRequest.class))).thenReturn(stagesF);

    // when
    var tableF = tableService.getTable(getTableReq);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
    verify(stageServiceMock, never()).getTableStages(any(ListTableStagesRequest.class));

    assertThrows(
        ExecutionException.class,
        () -> {
          tableF.toCompletableFuture().get(2, SECONDS);
        });
  }

  @Test
  public void shouldReturnUserTables()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var entities = newArrayList(tableEntity1, tableEntity2);
    var tablesSource = Source.from(entities);
    var listTablesReq = ListTablesRequest.builder().userId(user.getId()).build();

    when(tableRepositoryMock.getUserTables(any(UUID.class))).thenReturn(tablesSource);

    // when
    var tablesF = tableService.getTables(listTablesReq);

    // then
    verify(tableRepositoryMock).getUserTables(any(UUID.class));

    var tables = tablesF.toCompletableFuture().get(2, SECONDS);
    assertEquals(entities.size(), tables.size());

    IntStream.range(0, entities.size()).forEach((i) -> assertTable(entities.get(i), tables.get(i)));
  }
  /*
      @Test
      public void shouldSaveTable() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        var newTable = new NewTable("some stuff", "table with my stuff");
        Sink<TableEntity, NotUsed> sink = Sink.fold(Done.done(), (Done d, TableEntity k) -> d);
        var newTableReq =
            NewTableRequest.builder()
                .userId(user.getId())
                .title(newTable.getTitle())
                .description(newTable.getDescription())
                .build();

        when(tableRepositoryMock.saveTable()).thenReturn(sink);

        // when
        var tableF = tableService.saveTable(newTableReq);

        // then
        verify(tableRepositoryMock).saveTable();

        var table = tableF.toCompletableFuture().get(2, SECONDS);
        assertEquals(newTable.getTitle(), table.getTitle());
        assertEquals(newTable.getDescription(), table.getDescription());
      }
  /*
      @Test
      public void shouldUpdateTable()
          throws InterruptedException, ExecutionException, TimeoutException {
        // given
        var sink = Sink.fold(Done.done(), (Done d, TableEntity k) -> d);
        var updateTableReq =
            UpdateTableRequest.builder()
                .userId(user.getId())
                .tableKey(tableKey1)
                .title("new title")
                .description("new descryption")
                .build();

        when(tableRepositoryMock.updateTable()).thenReturn(sink);

        // when
        var tableF = tableService.updateTable(updateTableReq);

        // then
        verify(tableRepositoryMock).updateTable();

        var table = tableF.toCompletableFuture().get(2, SECONDS);
        assertEquals(updateTableReq.getTitle(), table.getTitle());
        assertEquals(updateTableReq.getDescription(), table.getDescription());
      }

      @Test
      public void shouldDeleteTable()
          throws InterruptedException, ExecutionException, TimeoutException {
        // given
        Sink<TableKey, NotUsed> sink = Sink.
        var deleteTableReq =
            DeleteTableRequest.builder().userId(user.getId()).tableKey(tableKey1).build();

        when(tableRepositoryMock.deleteTable(any(UUID.class))).thenReturn(sink);

        // when
        var tableKeyF = tableService.deleteTable(deleteTableReq);

        // then
        verify(tableRepositoryMock).deleteTable(any(UUID.class));

        var tableKey = tableKeyF.toCompletableFuture().get(2, SECONDS);
        assertEquals(tableKey1, tableKey);
      }
    */
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
