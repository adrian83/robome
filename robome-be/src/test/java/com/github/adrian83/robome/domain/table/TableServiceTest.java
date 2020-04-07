package com.github.adrian83.robome.domain.table;

import static java.time.LocalDateTime.now;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.stage.model.Stage;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.TableState;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.common.collect.Lists;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;

public class TableServiceTest {

  private TableRepository tableRepositoryMock = Mockito.mock(TableRepository.class);
  private StageService stageServiceMock = Mockito.mock(StageService.class);
  private ActorSystem actorSystem = ActorSystem.create();

  private TableService tableService =
      new TableService(tableRepositoryMock, stageServiceMock, actorSystem);

  private User user = new User("johndoe@somedomain.com", "fbwyflwegrl", Lists.newArrayList());
  private TableKey tableKey = new TableKey();
  private TableEntity tableEntity =
      new TableEntity(
          tableKey, user.getId(), "test", "test table", TableState.ACTIVE, now(), now());
  private Stage stage = new Stage(tableKey, user.getId(), "stage");

  @Test
  public void shouldReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stages = Lists.newArrayList(stage);
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(stages);

    var tableSource = Source.lazySingle(() -> Optional.of(tableEntity));

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(User.class), any(TableKey.class))).thenReturn(stagesF);

    // when
    var maybeTableF = tableService.getTable(user, tableKey);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
    verify(stageServiceMock).getTableStages(any(User.class), any(TableKey.class));

    var maybeTable = maybeTableF.toCompletableFuture().get(2, TimeUnit.SECONDS);
    assertTrue(maybeTable.isPresent());

    var resultTable = maybeTable.get();
    assertTable(tableEntity, resultTable);

    assertEquals(1, resultTable.getStages().size());
    var resultStage = resultTable.getStages().get(0);

    assertStage(stage, resultStage);
  }

  @Test
  public void shouldNotReturnTableForGivenKeyAndUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    // given
    var stagesF = CompletableFuture.<List<Stage>>completedFuture(Lists.newArrayList());
    var tableSource = Source.lazySingle(() -> Optional.<TableEntity>empty());

    when(tableRepositoryMock.getById(any(UUID.class), any(UUID.class))).thenReturn(tableSource);
    when(stageServiceMock.getTableStages(any(User.class), any(TableKey.class))).thenReturn(stagesF);

    // when
    var maybeTableF = tableService.getTable(user, tableKey);

    // then
    verify(tableRepositoryMock).getById(any(UUID.class), any(UUID.class));
    verify(stageServiceMock, never()).getTableStages(any(User.class), any(TableKey.class));

    var maybeTable = maybeTableF.toCompletableFuture().get(2, TimeUnit.SECONDS);
    assertFalse(maybeTable.isPresent());
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
