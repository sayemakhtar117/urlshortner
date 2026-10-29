package com.sayem.urlshortener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sayem.urlshortener.repository.UrlMappingRepository;
import com.sayem.urlshortener.service.RedirectService;

@ExtendWith(MockitoExtension.class)
class RedirectServiceTest {
	@Mock
	private UrlMappingRepository repository;
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private ValueOperations<String, String> valueOperations;
	@InjectMocks
	private RedirectService service;

	@Test
	void testGetOriginalUrlFromCache() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("abc")).thenReturn("https://example.com");
		when(repository.findById("abc")).thenReturn(Optional.empty());
		assertEquals("https://example.com", service.getOriginalUrl("abc"));
	}
}