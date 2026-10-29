package com.sayem.urlshortener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sayem.urlshortener.service.RedirectService;
import com.sayem.urlshortener.service.UrlShortenerService;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.security.user.name=test-user", "spring.security.user.password=test-password" })
class UrlShortenerControllerTest {
	@Autowired
	private TestRestTemplate restTemplate;
	@MockBean
	private UrlShortenerService urlShortenerService;
	@MockBean
	private RedirectService redirectService;

	@Test
	void testShortenAndRedirect() {
		when(urlShortenerService.shortenUrl("https://example.com")).thenReturn("abc123");
		when(redirectService.getOriginalUrl("abc123")).thenReturn("https://example.com");
		TestRestTemplate authenticatedRestTemplate = restTemplate.withBasicAuth("test-user", "test-password");
		ResponseEntity<String> shorten = authenticatedRestTemplate.postForEntity("/api/shorten",
				"https://example.com", String.class);
		assertEquals(HttpStatus.OK, shorten.getStatusCode());
		assertEquals("abc123", shorten.getBody());

		String code = shorten.getBody();
		ResponseEntity<Void> redirect = restTemplate.getForEntity("/" + code, Void.class);
		assertEquals(HttpStatus.FOUND, redirect.getStatusCode());
	}
}