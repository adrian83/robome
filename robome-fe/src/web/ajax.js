


class ResponseError extends Error {
    constructor(status, body, ...params) {
        super(...params);

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, ResponseError);
        }

        this.status = status;
        this.body = body;
    }
}

class ResponseWithBody {
    constructor(response, body) {
        this.response = response;
        this.body = body;
    }
}


function headers() {
    return {
        "Content-type": "application/json; charset=UTF-8"
    }
}

function headersWithAuthToken(authToken) {
    var hds = headers();
    hds["Authorization"] = authToken;
    return hds;
}

function handleServerSideError(response) {

    if(response.ok){
        return response;
    }

    if(response.status === 500){
        throw new ResponseError(response.status, "server side error");
    } else if(response.status === 400){
        return response.json().then(data => new ResponseWithBody(response, data));
    } else {
        throw new ResponseError(response.status, "unknown response");
    }
}

function handleBadRequestResponse(responseWithBody){
    
    if(responseWithBody.ok){
        return responseWithBody;
    }

    throw new ResponseError(responseWithBody.response.status, responseWithBody.body);
}


export function securedPost(url, authToken, data) {
    return fetch(url, {
        method: 'POST',
        mode: 'cors',
        body: JSON.stringify(data),
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function securedPut(url, authToken, data) {
    return fetch(url, {
        method: 'PUT',
        mode: 'cors',
        body: JSON.stringify(data),
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function unsecuredPost(url, data) {
    return fetch(url, {
        method: 'POST',
        mode: 'cors',
        body: JSON.stringify(data),
        headers: headers()
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export default function securedGet(url, authToken) {
    return fetch(url, {
        method: 'GET',
        mode: 'cors',
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function unsecuredGet(url) {
    return fetch(url, {
        method: 'GET',
        mode: 'cors',
        headers: headers()
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

