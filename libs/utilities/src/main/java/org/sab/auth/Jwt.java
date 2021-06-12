package org.sab.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Jwt {
    /**
     * The Secret used for signing the token.
     */
    private static final String SECRET = "scaleabull";

    private static final String ISSUER = "org.sab";

    private static final Algorithm ALGORITHM = Algorithm.HMAC512(SECRET);

    private static final JWTVerifier JWT_VERIFIER = JWT.require(ALGORITHM).withIssuer(ISSUER).build();

    /**
     * Generates a JWT Token given the claims and the expiration duration in minutes.
     *
     * @param claims            The claims to add to the JWT Payload
     * @param expirationMinutes The expiration duration in minutes
     * @return A String representing the generated token
     */
    public static String generateToken(Map<String, String> claims, long expirationMinutes) {
        return createJwtWithClaims(claims)
                .withIssuer(ISSUER)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000))
                .sign(Algorithm.HMAC512(SECRET));
    }

    /**
     * This method will verify and decode the JWT, returning a Map of String claims. If the JWT is
     * invalid or expired this will throw a JWTVerificationException.
     *
     * @param token A String representing the token
     * @return The decoded claims
     */
    public static Map<String, Object> verifyAndDecode(String token) {
        DecodedJWT decoded = JWT_VERIFIER.verify(token);
        Map<String, Object> claims = new HashMap<>();
        Map<String, Claim> decodedClaims = decoded.getClaims();
        for (String key : decodedClaims.keySet()) {
            if (key.equals("exp") || key.equals("iat")) {
                claims.put(key, decodedClaims.get(key).asDate());
            } else {
                claims.put(key, decodedClaims.get(key).asString());
            }
        }
        return claims;
    }

    /**
     * This method create the JWT Builder and adds the claims to the JWT payload.
     *
     * @param claims The claims to be added to the JWT Payload.
     * @return The JWTCreator.Builder
     */
    private static JWTCreator.Builder createJwtWithClaims(Map<String, String> claims) {
        JWTCreator.Builder builder = JWT.create();
        for (String key : claims.keySet()) {
            builder.withClaim(key, claims.get(key));
        }
        return builder;
    }
}
