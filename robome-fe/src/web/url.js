

export function tablesBeUrl() {
    return `/tables`;
}

export function tableBeUrl(tableId) {
    return `/tables/${tableId}`;
}

export function stageBeUrl(tableId, stageId) {
    return `/tables/${tableId}/stages/${stageId}`;
}

export function stagesBeUrl(tableId) {
    return `/tables/${tableId}/stages`;
}

export function activityBeUrl(tableId, stageId, activityId) {
    return `/tables/${tableId}/stages/${stageId}/activities/${activityId}`;
}

export function activitiesBeUrl(tableId, stageId) {
    return `/tables/${tableId}/stages/${stageId}/activities`;
}

export function loginBeUrl() { 
    return `/auth/login`;
}

export function registerBeUrl() {
    return `/auth/register`;
}

export function editTableUrl(tableId) {
    return `/tables/edit/${tableId}`;
}

export function showTableUrl(tableId) {
    return `/tables/show/${tableId}`;
}

export function createTableUrl() {
    return `/tables/create/`;
}

export function listTablesUrl() {
    return `/tables/list/`;
}

export function createStageUrl(tableId) {
    return `/tables/show/${tableId}/stages/create`;
}

export function createActivityUrl(tableId, stageId) {
    return `/tables/show/${tableId}/stages/show/${stageId}/activities/create`;
}

export function editStageUrl(tableId, stageId) {
    return `/tables/show/${tableId}/stages/edit/${stageId}`;
}

export function editActivityUrl(tableId, stageId, activityId) {
    return `/tables/show/${tableId}/stages/show/${stageId}/activities/edit/${activityId}`;
}

export function loginUrl() {
    return `/login/`;
}

export function logoutUrl() {
    return `/logout/`;
}

export function registerUrl() {
    return `/register/`;
}

export function healthUrl() {
    return `/health/`;
}