package com.github.adrian83.robome.domain.table;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;

public class TableServiceTest {

  private TableRepository tableRepositoryMock = Mockito.mock(TableRepository.class);
  private StageService stageServiceMock = Mockito.mock(StageService.class);
  private ActorSystem actorSystem = ActorSystem.create();

  private TableService tableService =
      new TableService(tableRepositoryMock, stageServiceMock, actorSystem);

  private UserData user =
      new UserData(UUID.randomUUID(), "johndoe@somedomain.com", Collections.emptySet());

  private TableKey tableKey1 = TableKey.random(user.id());
  private TableKey tableKey2 = TableKey.random(user.id());

  private TableEntity tableEntity1 =
      new TableEntity(
          tableKey1,
          "test 1",
          "test table 1",
          TableState.ACTIVE,
          Time.utcNow(),
          Time.utcNow());

  private TableEntity tableEntity2 =
      new TableEntity(
          tableKey2,
          "test 2",
          "test table 2",
          TableState.ACTIVE,
          Time.utcNow(),
          Time.utcNow());

  private Stage stage =
      new Stage(
          StageKey.randomWithTableKey(tableKey1),
          "stage 1",
          StageState.ACTIVE,
          Time.utcNow(),
          Time.utcNow(),
          Collections.emptyList());

  @Test
  public void shouldReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stages = newArrayList(stage);
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(stages);
    var getTableReq = new GetTableRequest(user.id(), tableKey1);

    var tableSource = Source.lazySingle(() -> tableEntity1);

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(ListTableStagesRequest.class))).thenReturn(stagesF);

    // when
    var maybeTableF = tableService.getTable(getTableReq);
    var maybeTable = maybeTableF.toCompletableFuture().get(1, SECONDS);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
    verify(stageServiceMock).getTableStages(any(ListTableStagesRequest.class));

    assertThat(maybeTable).isNotEmpty();

    var table = maybeTable.get();

    assertTable(tableEntity1, table);
    assertThat(table.stages()).hasSize(1);
    assertStage(stage, table.stages().get(0));
  }

  @Test()
  public void shouldNotReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    // var stagesF = CompletableFuture.<List<Stage>>completedFuture(newArrayList());
    var getTableReq = new GetTableRequest(user.id(), tableKey1);

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(Source.empty());
    // when(stageServiceMock.getTableStages(any(ListTableStagesRequest.class))).thenReturn(stagesF);

    // when
    var maybeTableF = tableService.getTable(getTableReq);
    var maybeTable = maybeTableF.toCompletableFuture().get(1, SECONDS);

    // then
    assertThat(maybeTable).isEmpty();
  }

  @Test
  public void shouldReturnUserTables()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var entities = newArrayList(tableEntity1, tableEntity2);
    var tablesSource = Source.from(entities);
    var listTablesReq = new ListTablesRequest(user.id());

    when(tableRepositoryMock.getUserTables(any(UUID.class))).thenReturn(tablesSource);

    // when
    var tablesF = tableService.getTables(listTablesReq);
    var tables = tablesF.toCompletableFuture().get(1, SECONDS);

    // then
    verify(tableRepositoryMock).getUserTables(any(UUID.class));

    assertThat(entities).hasSize(tables.size());

    assertThat(entities)
        .anySatisfy(
            tableEntity -> assertThat(tables).anySatisfy(table -> assertTable(tableEntity, table)));
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
    assertThat(expected.key()).isEqualTo(actual.key());
    assertThat(expected.title()).isEqualTo(actual.title());
    assertThat(expected.description()).isEqualTo(actual.description());
    assertThat(expected.state()).isEqualTo(actual.state());
    assertThat(expected.createdAt()).isEqualTo(actual.createdAt());
    assertThat(expected.modifiedAt()).isEqualTo(actual.modifiedAt());
  }

  public void assertStage(Stage expected, Stage actual) {
    assertThat(expected.key()).isEqualTo(actual.key());
    assertThat(expected.title()).isEqualTo(actual.title());
    assertThat(expected.state()).isEqualTo(actual.state());
    assertThat(expected.createdAt()).isEqualTo(actual.createdAt());
    assertThat(expected.modifiedAt()).isEqualTo(actual.modifiedAt());
  }
}
