package com.github.gquintana.kafka.brod.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwtServiceTest {

    private JwtService jwtService = new JwtService("kafka-brod", SignatureAlgorithm.HS256, null);

    @Test
    public void testCreateParse() {
        String token = jwtService.createToken("user");
        assertThat(token, notNullValue());
        String userName = jwtService.parseToken(token);
        assertThat(userName, equalTo("user"));
    }

    @Test
    public void testCreateCreate() throws InterruptedException {
        String token = jwtService.createToken("user");
        String token2 = jwtService.createToken("user");
        assertThat(token, not(equalTo(token2)));
    }

    @Test(expected = JwtException.class)
    public void testCreateBreakParse() {
        String token = jwtService.createToken("user");
        assertThat(token, notNullValue());
        token = token.replaceAll("A", "B");
        jwtService.parseToken(token);
    }

    @Test(expected = JwtException.class)
    public void testParseNode() {
        String token = Jwts.builder().setIssuer("kafka-brod").setSubject("robert").compact();
        String s = jwtService.parseToken(token);
    }

}
