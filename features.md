## AUTH system 

1. Database Changes:

  - Add a new table in Cassandra to store refresh tokens with fields:

    - token_id (UUID)
    - user_id (UUID)
    - refresh_token (String)
    - issued_at (Timestamp)
    - expires_at (Timestamp)

2. Model Changes:

  - Create new classes:

    - RefreshToken.java - Entity representing refresh token
    - TokenPair.java - DTO containing both access and refresh tokens
    - RefreshTokenRequest.java - Request model for token refresh
    - TokenResponse.java - Response model with token information

3. Service Layer Changes:

  - Create RefreshTokenService.java:

    - generateRefreshToken(UserData) - Creates new refresh token
    - validateRefreshToken(String) - Validates token and returns associated user


4. Authentication Class Changes:

  - Modify Authentication.java:

    - Add refresh token generation to login flow
    - Add method to handle refresh token requests
    - Reduce access token expiration time (e.g., to 15 minutes)
    - Add method to revoke refresh tokens on logout

5. Controller Changes:

  - Update AuthController.java:

    - Add /refresh endpoint to get new access token using refresh token
    - Modify login endpoint to return both tokens
    - Add logout endpoint to revoke refresh tokens
    - Update Security filter to handle new token structure

6. Security Enhancements:

  - Implement token rotation (new refresh token with each refresh)
  - Add refresh token family concept to detect token reuse
  - Add rate limiting for token endpoints
  - Store token hashes instead of raw tokens
