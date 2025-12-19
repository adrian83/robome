package com.github.adrian83.robome.web.auth;

import java.util.function.Function;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.auth.model.UserData;

public final class Authorization {

    private Authorization() {
    }

    public static UserData canReadTables(UserData userData) {
        boolean hasPermision = can(userData, PermissionChecker::canReadTables);
        if (hasPermision) {
            return userData;
        }
        throw new UserNotAuthorizedException("user cannot read tables");
    }

    public static UserData canReadStages(UserData userData) {
        boolean hasPermision = can(userData, PermissionChecker::canReadStages);
        if (hasPermision) {
            return userData;
        }
        throw new UserNotAuthorizedException("user cannot read stages");
    }

    public static UserData canWriteStages(UserData userData) {
        boolean hasPermision = can(userData, PermissionChecker::canWriteStages);
        if (hasPermision) {
            return userData;
        }
        throw new UserNotAuthorizedException("user cannot write stages");
    }

    public static UserData canReadAcivities(UserData userData) {
        boolean hasPermision = can(userData, PermissionChecker::canReadAcivities);
        if (hasPermision) {
            return userData;
        }
        throw new UserNotAuthorizedException("user cannot read activities");
    }

    public static UserData canWriteAcivities(UserData userData) {
        boolean hasPermision = can(userData, PermissionChecker::canWriteAcivities);
        if (hasPermision) {
            return userData;
        }
        throw new UserNotAuthorizedException("user cannot write activities");
    }

    public static UserData canWriteTables(UserData userData) {
        boolean hasPermision = can(userData, PermissionChecker::canWriteTables);
        if (hasPermision) {
            return userData;
        }
        throw new UserNotAuthorizedException("user cannot write tables");
    }

    private static boolean can(final UserData userData, final Function<UserData, Boolean> hasPermision) {
        return PermissionChecker.isAdmin(userData) || userCan(userData, hasPermision);
    }

    private static boolean userCan(final UserData userData, final Function<UserData, Boolean> hasPermision) {
        return hasPermision.apply(userData);
    }
}
