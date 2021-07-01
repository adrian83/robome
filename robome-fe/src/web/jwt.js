import jwt_decode from "jwt-decode";

function extractClaim(jwtToken, claim) {
    console.log("claim", claim, "jwtToken", jwtToken);
    var decoded = jwt_decode(jwtToken);
    console.log('decoded', decoded);
    var j = decoded[claim];
    console.log("claim", claim, "value", j);
    return j;
}

export function extractUserId(jwtToken){
    return extractClaim(jwtToken, "id");
}

export function extractExpirationTs(jwtToken){
    return extractClaim(jwtToken, "exp");
}

export default extractClaim;