package com.sayem.urlshortener.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sayem.urlshortener.model.UrlMapping;
import com.sayem.urlshortener.repository.UrlMappingRepository;

@Service
public class UrlShortenerService {
	private final UrlMappingRepository repository;
	private final RedisTemplate<String, String> redisTemplate;

	UrlShortenerService(UrlMappingRepository repository, RedisTemplate<String, String> redisTemplate) {
		this.repository = repository;
		this.redisTemplate = redisTemplate;
	}

	public String shortenUrl(String originalUrl) {
		String shortCode = UUID.randomUUID().toString().substring(0, 6);
		UrlMapping mapping = new UrlMapping();
		mapping.setId(shortCode);
		mapping.setOriginalUrl(originalUrl);
		mapping.setCreatedAt(LocalDateTime.now());
		mapping.setExpiryDate(LocalDateTime.now().plusDays(30));
		repository.save(mapping);
		redisTemplate.opsForValue().set(shortCode, originalUrl);
		return shortCode;
	}
}