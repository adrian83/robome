package com.github.adrian83.robome.domain.table;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.stage.model.StageKey;
import com.github.adrian83.robome.domain.stage.model.StageState;
import com.github.adrian83.robome.domain.stage.model.request.ListTableStagesQuery;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.TableState;
import com.github.adrian83.robome.domain.table.model.request.DeleteTableCommand;
import com.github.adrian83.robome.domain.table.model.request.GetTableQuery;
import com.github.adrian83.robome.domain.table.model.request.ListTablesQuery;
import com.github.adrian83.robome.domain.table.model.request.NewTableCommand;
import com.github.adrian83.robome.domain.table.model.request.UpdateTableCommand;
import com.github.adrian83.robome.web.table.model.NewTable;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

public class TableServiceTest {

    private final TableRepository tableRepositoryMock = Mockito.mock(TableRepository.class);
    private final StageService stageServiceMock = Mockito.mock(StageService.class);
    private final ActorSystem actorSystem = ActorSystem.create();

    private final TableService tableService = new TableService(tableRepositoryMock, stageServiceMock, actorSystem);

    private final UserData user = new UserData(UUID.randomUUID(), "johndoe@somedomain.com", Collections.emptySet());

    private final LocalDateTime now = Time.utcNow();

    @Test
    public void shouldReturnTableForGivenKeyAndUser() throws Exception {
        // given
        var tableKey = TableKey.create(user.id());
        var tableEntity = new TableEntity(tableKey, "test 1", "test table 1", TableState.ACTIVE, now, now);
        var stage = new Stage(StageKey.basedOnTableKey(tableKey), "stage 1", StageState.ACTIVE, now, now, Collections.emptyList());

        var stagesF = CompletableFuture.<List<Stage>>completedFuture(List.of(stage));
        var getTableReq = new GetTableQuery(user.id(), tableKey);

        var tableSource = Source.lazySingle(() -> tableEntity);

        when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
        when(stageServiceMock.getTableStages(any(ListTableStagesQuery.class))).thenReturn(stagesF);

        // when
        var maybeTable = tableService.getTable(getTableReq).toCompletableFuture().get(1, SECONDS);

        // then
        verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
        verify(stageServiceMock).getTableStages(any(ListTableStagesQuery.class));

        assertThat(maybeTable).isNotEmpty();

        var table = maybeTable.get();

        assertTable(tableEntity, table);
        assertThat(table.stages()).hasSize(1);
        assertStage(stage, table.stages().get(0));
    }

    @Test()
    public void shouldNotReturnTableForGivenKeyAndUser() throws Exception {
        // given
        var tableKey = TableKey.create(user.id());
        var getTableReq = new GetTableQuery(user.id(), tableKey);

        when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(Source.empty());

        // when
        var maybeTable = extract(tableService.getTable(getTableReq));

        // then
        assertThat(maybeTable).isEmpty();
    }

    @Test
    public void shouldReturnUserTables() throws Exception {
        // given
        var tableKey1 = TableKey.create(user.id());
        var tableKey2 = TableKey.create(user.id());

        var tableEntity1 = new TableEntity(tableKey1, "test 1", "test table 1", TableState.ACTIVE, now, now);
        var tableEntity2 = new TableEntity(tableKey2, "test 2", "test table 2", TableState.ACTIVE, now, now);

        var tableEntities = List.of(tableEntity1, tableEntity2);
        var tablesSource = Source.from(tableEntities);
        var listTablesReq = new ListTablesQuery(user.id());

        when(tableRepositoryMock.getUserTables(any(UUID.class))).thenReturn(tablesSource);

        // when
        var tables = extract(tableService.getTables(listTablesReq));

        // then
        verify(tableRepositoryMock).getUserTables(any(UUID.class));

        assertThat(tableEntities).hasSize(tables.size());

        assertThat(tableEntities)
                .anySatisfy(tableEntity -> assertThat(tables).anySatisfy(table -> assertTable(tableEntity, table)));
    }

    @Test
    public void shouldSaveTable() throws Exception {
        // given 
        var newTable = new NewTable("some stuff", "table with my stuff");
        var saveTableFlow = Flow.of(TableEntity.class);
        var newTableReq = new NewTableCommand(newTable.title(), newTable.description(), user.id());

        when(tableRepositoryMock.saveTable()).thenReturn(saveTableFlow);

        // when 
        var table = extract(tableService.saveTable(newTableReq));

        // then 
        verify(tableRepositoryMock).saveTable();

        assertThat(newTableReq.title()).isEqualTo(table.title());
        assertThat(newTableReq.description()).isEqualTo(table.description());
    }

    @Test
    public void shouldUpdateTable() throws Exception {
        // given 
        var tableKey = TableKey.create(user.id());
        var updateTableFlow = Flow.of(TableEntity.class);
        var updateTableReq = new UpdateTableCommand("new title", "new descryption", user.id(), tableKey);

        when(tableRepositoryMock.updateTable()).thenReturn(updateTableFlow);

        // when 
        var table = extract(tableService.updateTable(updateTableReq));

        // then 
        verify(tableRepositoryMock).updateTable();

        assertThat(updateTableReq.title()).isEqualTo(table.title());
        assertThat(updateTableReq.description()).isEqualTo(table.description());
    }

    @Test
    public void shouldDeleteTable() throws Exception {
        // given 
        var tableKey = TableKey.create(user.id());
        var deleteTableFlow = Flow.of(TableKey.class);
        var deleteTableReq = new DeleteTableCommand(user.id(), tableKey);

        when(tableRepositoryMock.deleteTable(any(UUID.class))).thenReturn(deleteTableFlow);

        // when 
        var deletedTableKey = extract(tableService.deleteTable(deleteTableReq));

        // then 
        verify(tableRepositoryMock).deleteTable(any(UUID.class));

        assertThat(deletedTableKey).isEqualTo(tableKey);
    }

    private <T> T extract(CompletionStage<T> cmplStage) throws Exception {
        return cmplStage.toCompletableFuture().get(1, SECONDS);
    }

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
