
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & (0x3 | 0x8));
      return v.toString(16);
    });
  }

class ResponseError extends Error {
    constructor(status, body, ...params) {
        super(...params);

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, ResponseError);
        }

        this.status = status;
        this.body = body;
        this.id = uuidv4();
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

function buildUrl(path){
    return process.env.REACT_APP_BACKEND_HOST + path;
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
    } else if(response.status === 401){
        throw new ResponseError(response.status, "unauthorized");
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
    return fetch(buildUrl(url), {
        method: 'POST',
        mode: 'cors',
        body: JSON.stringify(data),
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function securedDelete(url, authToken) {
    return fetch(buildUrl(url), {
        method: 'DELETE',
        mode: 'cors',
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function securedPut(url, authToken, data) {
    return fetch(buildUrl(url), {
        method: 'PUT',
        mode: 'cors',
        body: JSON.stringify(data),
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function unsecuredPost(url, data) {
    return fetch(buildUrl(url), {
        method: 'POST',
        mode: 'cors',
        body: JSON.stringify(data),
        headers: headers()
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export default function securedGet(url, authToken) {
    return fetch(buildUrl(url), {
        method: 'GET',
        mode: 'cors',
        headers: headersWithAuthToken(authToken)
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}

export function unsecuredGet(url) {
    return fetch(buildUrl(url), {
        method: 'GET',
        mode: 'cors',
        headers: headers()
    })
    .then(handleServerSideError)
    .then(handleBadRequestResponse);
}


