package com.github.adrian83.robome.web.auth;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        var admin = ownerWithRoles(Role.ADMIN);

        // when
        var result = Authorization.canReadTables(admin);

        // then
        assertThat(admin).isEqualTo(result);
    }

    @Test
    public void shouldAdminReadStages() {
        // given
        var admin = ownerWithRoles(Role.ADMIN);

        // when
        var result = Authorization.canReadStages(admin);

        // then
        assertThat(admin).isEqualTo(result);
    }

    @Test
    public void shouldAdminWriteTables() {
        // given
        var admin = ownerWithRoles(Role.ADMIN);

        // when
        var result = Authorization.canWriteTables(admin);

        // then
        assertThat(admin).isEqualTo(result);
    }

    @Test
    public void shouldAdminWriteStages() {
        // given
        var admin = ownerWithRoles(Role.ADMIN);

        // when
        var result = Authorization.canWriteStages(admin);

        // then
        assertThat(admin).isEqualTo(result);
    }

    @Test
    public void shouldCheckIfUserCanReadStages() {
        // given
        var userWithRole = ownerWithRoles(Role.READ_STAGES);

        // when
        var result = Authorization.canReadStages(userWithRole);

        // then
        assertThat(userWithRole).isEqualTo(result);
    }

    @Test
    public void shouldThrowExceptionIfUserCanNotReadStages() {
        // given
        var userWithoutRole = ownerWithRoles();

        // when
        assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
                .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    public void shouldCheckIfUserCanReadTables() {
        // given
        var userWithRole = ownerWithRoles(Role.READ_TABLES);

        // when
        var result = Authorization.canReadTables(userWithRole);

        // then
        assertThat(userWithRole).isEqualTo(result);
    }

    @Test
    public void shouldThrowExceptionIfUserCanNotReadTables() {
        // given
        var userWithoutRole = ownerWithRoles();

        // when
        assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
                .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    public void shouldCheckIfUserCanWriteStages() {
        // given
        var userWithRole = ownerWithRoles(Role.WRITE_STAGES);

        // when
        var result = Authorization.canWriteStages(userWithRole);

        // then
        assertThat(userWithRole).isEqualTo(result);
    }

    @Test
    public void shouldThrowExceptionIfUserCanNotWriteStages() {
        // given
        var userWithoutRole = ownerWithRoles();

        // when
        assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
                .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    public void shouldCheckIfUserCanWriteTables() {
        // given
        var userWithRole = ownerWithRoles(Role.WRITE_TABLES);

        // when
        var result = Authorization.canWriteTables(userWithRole);

        // then
        assertThat(userWithRole).isEqualTo(result);
    }

    @Test
    public void shouldThrowExceptionIfUserCanNotWriteTables() {
        // given
        var userWithoutRole = ownerWithRoles();

        // when
        assertThatThrownBy(() -> Authorization.canReadStages(userWithoutRole))
                .isInstanceOf(UserNotAuthorizedException.class);
    }

    private UserContext ownerWithRoles(Role... roles) {
        var userId = UUID.randomUUID();
        var userData = new UserData(userId, "johndoe@somedomain.com", Sets.newHashSet(roles));

        return new UserContext(userData, Optional.of(userId));
    }
}
