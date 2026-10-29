package com.sayem.urlshortener.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final Key signingKey;

	public JwtUtil(@Value("${app.jwt.secret}") String secret) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
				.signWith(signingKey, SignatureAlgorithm.HS256).compact();
	}

	public String validateToken(String token) {
		return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody().getSubject();
	}
}