package com.github.adrian83.robome.web.auth;

import java.util.function.Function;

import com.github.adrian83.robome.auth.exception.UserNotAuthorizedException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.common.UserContext;

public final class Authorization {

    private Authorization() {
    }

    public static UserContext canReadTables(UserContext userCtx) {
        boolean hasPermision = can(userCtx, PermissionChecker::canReadTables);
        if (hasPermision) {
            return userCtx;
        }
        throw new UserNotAuthorizedException("user cannot read tables");
    }

    public static UserContext canReadStages(UserContext userCtx) {
        boolean hasPermision = can(userCtx, PermissionChecker::canReadStages);
        if (hasPermision) {
            return userCtx;
        }
        throw new UserNotAuthorizedException("user cannot read stages");
    }

    public static UserContext canWriteStages(UserContext userCtx) {
        boolean hasPermision = can(userCtx, PermissionChecker::canWriteStages);
        if (hasPermision) {
            return userCtx;
        }
        throw new UserNotAuthorizedException("user cannot write stages");
    }

    public static UserContext canReadAcivities(UserContext userCtx) {
        boolean hasPermision = can(userCtx, PermissionChecker::canReadAcivities);
        if (hasPermision) {
            return userCtx;
        }
        throw new UserNotAuthorizedException("user cannot read activities");
    }

    public static UserContext canWriteAcivities(UserContext userCtx) {
        boolean hasPermision = can(userCtx, PermissionChecker::canWriteAcivities);
        if (hasPermision) {
            return userCtx;
        }
        throw new UserNotAuthorizedException("user cannot write activities");
    }

    public static UserContext canWriteTables(UserContext userCtx) {
        boolean hasPermision = can(userCtx, PermissionChecker::canWriteTables);
        if (hasPermision) {
            return userCtx;
        }
        throw new UserNotAuthorizedException("user cannot write tables");
    }

    private static boolean can(final UserContext userCtx, final Function<UserData, Boolean> hasPermision) {
        return PermissionChecker.isAdmin(userCtx.loggedInUser()) || userCan(userCtx, hasPermision);
    }

    private static boolean userCan(final UserContext userCtx, final Function<UserData, Boolean> hasPermision) {
        return hasPermision.apply(userCtx.loggedInUser()) && userCtx.userOwnsResource();
    }
}
