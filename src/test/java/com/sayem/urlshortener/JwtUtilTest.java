package com.sayem.urlshortener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sayem.urlshortener.security.JwtUtil;

class JwtUtilTest {
	private final JwtUtil jwtUtil = new JwtUtil("test-signing-key-with-at-least-32-bytes-long");

	@Test
	void testGenerateAndValidateToken() {
		String token = jwtUtil.generateToken("user");
		assertEquals("user", jwtUtil.validateToken(token));
	}
}