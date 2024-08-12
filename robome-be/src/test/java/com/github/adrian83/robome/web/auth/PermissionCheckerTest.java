package com.github.adrian83.robome.web.auth;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.user.model.Role;
import com.google.common.collect.Sets;

public class PermissionCheckerTest {

    @Test
    public void shouldAdminHaveAllPermissions() {
        // given
        var admin = userWithRoles(Role.ADMIN);

        // when, then
        assertThat(PermissionChecker.canReadStages(admin)).isTrue();
        assertThat(PermissionChecker.canReadTables(admin)).isTrue();
        assertThat(PermissionChecker.canWriteStages(admin)).isTrue();
        assertThat(PermissionChecker.canWriteTables(admin)).isTrue();
    }

    @Test
    public void shouldCheckIfUserHasPermissionToReadStages() {
        // given
        var userWithoutRole = userWithRoles();
        var userWithRole = userWithRoles(Role.READ_STAGES);

        // when, then
        assertThat(PermissionChecker.canReadStages(userWithoutRole)).isFalse();
        assertThat(PermissionChecker.canReadStages(userWithRole)).isTrue();
    }

    @Test
    public void shouldCheckIfUserHasPermissionToReadTables() {
        // given
        var userWithoutRole = userWithRoles();
        var userWithRole = userWithRoles(Role.READ_TABLES);

        // when, then
        assertThat(PermissionChecker.canReadTables(userWithoutRole)).isFalse();
        assertThat(PermissionChecker.canReadTables(userWithRole)).isTrue();
    }

    @Test
    public void shouldCheckIfUserHasPermissionToWriteStages() {
        // given
        var userWithoutRole = userWithRoles();
        var userWithRole = userWithRoles(Role.WRITE_STAGES);

        // when, then
        assertThat(PermissionChecker.canWriteStages(userWithoutRole)).isFalse();
        assertThat(PermissionChecker.canWriteStages(userWithRole)).isTrue();
    }

    @Test
    public void shouldCheckIfUserHasPermissionToWriteTables() {
        // given
        var userWithoutRole = userWithRoles();
        var userWithRole = userWithRoles(Role.WRITE_TABLES);

        // when, then
        assertThat(PermissionChecker.canWriteTables(userWithoutRole)).isFalse();
        assertThat(PermissionChecker.canWriteTables(userWithRole)).isTrue();
    }

    private UserData userWithRoles(Role... roles) {
        return new UserData(UUID.randomUUID(), "johndoe@somedomain.com", Sets.newHashSet(roles));
    }
}
