package com.github.adrian83.robome.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.common.UserContext;
import com.github.adrian83.robome.domain.user.model.Role;
import com.google.common.collect.Sets;

public class AuthorizationTest {

  @Test
  public void shouldAdminReadTables() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canReadTables(admin);

    // then
    assertEquals(admin, result);
  }

  public void shouldAdminReadStages() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canReadStages(admin);

    // then
    assertEquals(admin, result);
  }

  public void shouldAdminWriteTables() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canWriteTables(admin);

    // then
    assertEquals(admin, result);
  }

  public void shouldAdminWriteStages() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canWriteStages(admin);

    // then
    assertEquals(admin, result);
  }

  @Test
  public void shouldCheckIfUserCanReadStages() {
    // given
    var userWithRole = userWithRoles(Role.READ_STAGES);

    // when
    var result = Authorization.canReadStages(userWithRole);

    // then
    assertEquals(userWithRole, result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotReadStages() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    Assertions.assertThrows(
        UserNotAuthorizedException.class,
        () -> {
          Authorization.canReadStages(userWithoutRole);
        });
  }

  @Test
  public void shouldCheckIfUserCanReadTables() {
    // given
    var userWithRole = userWithRoles(Role.READ_TABLES);

    // when
    var result = Authorization.canReadTables(userWithRole);

    // then
    assertEquals(userWithRole, result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotReadTables() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    Assertions.assertThrows(
        UserNotAuthorizedException.class,
        () -> {
          Authorization.canReadTables(userWithoutRole);
        });
  }

  @Test
  public void shouldCheckIfUserCanWriteStages() {
    // given
    var userWithRole = userWithRoles(Role.WRITE_STAGES);

    // when
    var result = Authorization.canWriteStages(userWithRole);

    // then
    assertEquals(userWithRole, result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotWriteStages() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    Assertions.assertThrows(
        UserNotAuthorizedException.class,
        () -> {
          Authorization.canWriteStages(userWithoutRole);
        });
  }

  @Test
  public void shouldCheckIfUserCanWriteTables() {
    // given
    var userWithRole = userWithRoles(Role.WRITE_TABLES);

    // when
    var result = Authorization.canWriteTables(userWithRole);

    // then
    assertEquals(userWithRole, result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotWriteTables() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    Assertions.assertThrows(
        UserNotAuthorizedException.class,
        () -> {
          Authorization.canWriteTables(userWithoutRole);
        });
  }

  private UserContext userWithRoles(Role... roles) {
    var userData =
        UserData.builder()
            .id(UUID.randomUUID())
            .email("johndoe@somedomain.com")
            .roles(Sets.newHashSet(roles))
            .build();
    return new UserContext(userData, Optional.empty());
  }
}
