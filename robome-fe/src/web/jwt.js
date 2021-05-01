import jwt_decode from "jwt-decode";

function extractClaim(jwtToken, claim) {
    var decoded = jwt_decode(jwtToken);
    //console.log('decoded', decoded);
    return decoded[claim];
}

export function extractUserId(jwtToken){
    return extractClaim(jwtToken, "id");
}

export function extractExpirationTs(jwtToken){
    return extractClaim(jwtToken, "exp");
}

export default extractClaim;