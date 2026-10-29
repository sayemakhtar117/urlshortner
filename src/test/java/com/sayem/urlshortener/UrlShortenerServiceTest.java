package com.sayem.urlshortener;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.sayem.urlshortener.repository.UrlMappingRepository;
import com.sayem.urlshortener.service.UrlShortenerService;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {
	@Mock
	private UrlMappingRepository repository;
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private ValueOperations<String, String> valueOperations;
	@InjectMocks
	private UrlShortenerService service;

	@Test
	void testShortenUrl() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		String result = service.shortenUrl("https://example.com");
		assertNotNull(result);
		verify(repository, times(1)).save(any(com.sayem.urlshortener.model.UrlMapping.class));
		verify(valueOperations).set(anyString(), eq("https://example.com"));
	}
}