package com.github.gquintana.kafka.brod.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JwtService {
    private final String issuer;
    private final SignatureAlgorithm signatureAlgorithm;
    private final Key key;

    public JwtService(String issuer, SignatureAlgorithm signatureAlgorithm, Key key) {
        this.issuer = issuer;
        this.signatureAlgorithm = signatureAlgorithm;
        if (key == null) {
            key = MacProvider.generateKey(signatureAlgorithm);
        }
        this.key = key;
    }

    private SecureRandom random = new SecureRandom();

    public String createToken(String userName) {
        Instant now = Instant.now();
        String id = Long.toHexString(random.nextLong());
        return Jwts.builder()
            .setSubject(userName)
            .setIssuer(issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(30, ChronoUnit.MINUTES)))
            .setId(id)
            .signWith(signatureAlgorithm, key)
            .compact();
    }

    public String parseToken(String token) {
        Jws<Claims> claims = Jwts.parser()
            .setSigningKey(key)
            .requireIssuer(issuer)
            .parseClaimsJws(token);
        return claims.getBody().getSubject();
    }
}
