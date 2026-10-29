package com.sayem.urlshortener.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sayem.urlshortener.repository.UrlMappingRepository;

@Service
public class RedirectService {
	private final UrlMappingRepository repository;
	private final RedisTemplate<String, String> redisTemplate;

	RedirectService(UrlMappingRepository repository, RedisTemplate<String, String> redisTemplate) {
		this.repository = repository;
		this.redisTemplate = redisTemplate;
	}

	public String getOriginalUrl(String shortCode) {
		String cachedUrl = redisTemplate.opsForValue().get(shortCode);
		if (cachedUrl != null) {
			incrementClickCount(shortCode);
			return cachedUrl;
		}
		return repository.findById(shortCode).map(mapping -> {
			redisTemplate.opsForValue().set(shortCode, mapping.getOriginalUrl());
			incrementClickCount(shortCode);
			return mapping.getOriginalUrl();
		}).orElseThrow(() -> new RuntimeException("URL not found"));
	}

	private void incrementClickCount(String shortCode) {
		repository.findById(shortCode).ifPresent(mapping -> {
			mapping.setClickCount(mapping.getClickCount() + 1);
			repository.save(mapping);
		});
	}
}