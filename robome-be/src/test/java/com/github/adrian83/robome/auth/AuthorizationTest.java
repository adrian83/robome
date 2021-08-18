package com.github.adrian83.robome.auth;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.common.UserContext;
import com.github.adrian83.robome.domain.user.model.Role;
import com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthorizationTest {

  @Test
  public void shouldAdminReadTables() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canReadTables(admin);

    // then
    assertThat(admin).isEqualTo(result);
  }

  public void shouldAdminReadStages() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canReadStages(admin);

    // then
    assertThat(admin).isEqualTo(result);
  }

  public void shouldAdminWriteTables() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canWriteTables(admin);

    // then
    assertThat(admin).isEqualTo(result);
  }

  public void shouldAdminWriteStages() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when
    var result = Authorization.canWriteStages(admin);

    // then
    assertThat(admin).isEqualTo(result);
  }

  @Test
  public void shouldCheckIfUserCanReadStages() {
    // given
    var userWithRole = userWithRoles(Role.READ_STAGES);

    // when
    var result = Authorization.canReadStages(userWithRole);

    // then
    assertThat(userWithRole).isEqualTo(result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotReadStages() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
        .isInstanceOf(UserNotAuthorizedException.class);
  }

  @Test
  public void shouldCheckIfUserCanReadTables() {
    // given
    var userWithRole = userWithRoles(Role.READ_TABLES);

    // when
    var result = Authorization.canReadTables(userWithRole);

    // then
    assertThat(userWithRole).isEqualTo(result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotReadTables() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
        .isInstanceOf(UserNotAuthorizedException.class);
  }

  @Test
  public void shouldCheckIfUserCanWriteStages() {
    // given
    var userWithRole = userWithRoles(Role.WRITE_STAGES);

    // when
    var result = Authorization.canWriteStages(userWithRole);

    // then
    assertThat(userWithRole).isEqualTo(result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotWriteStages() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
        .isInstanceOf(UserNotAuthorizedException.class);
  }

  @Test
  public void shouldCheckIfUserCanWriteTables() {
    // given
    var userWithRole = userWithRoles(Role.WRITE_TABLES);

    // when
    var result = Authorization.canWriteTables(userWithRole);

    // then
    assertThat(userWithRole).isEqualTo(result);
  }

  @Test
  public void shouldThrowExceptionIfUserCanNotWriteTables() {
    // given
    var userWithoutRole = userWithRoles();

    // when
    assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
        .isInstanceOf(UserNotAuthorizedException.class);
  }

  private UserContext userWithRoles(Role... roles) {
    var userData = new UserData(
    		UUID.randomUUID(), 
    		"johndoe@somedomain.com",
            Sets.newHashSet(roles));
    
    return new UserContext(userData, Optional.empty());
  }
}
