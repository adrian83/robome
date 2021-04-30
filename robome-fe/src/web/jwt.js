import jwt_decode from "jwt-decode";

function extractClaim(jwtToken, claim) {
    var decoded = jwt_decode(jwtToken);
    return decoded[claim];
}

export function extractUserId(jwtToken){
    return extractClaim(jwtToken, "id");
}

export default extractClaim;