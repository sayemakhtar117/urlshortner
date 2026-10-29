package com.sayem.urlshortener.config;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.sayem.urlshortener.repository.UrlMappingRepository;

@Configuration
public class CleanupScheduler {
	
	private final UrlMappingRepository repository;

	CleanupScheduler(UrlMappingRepository repository) {
		this.repository = repository;
	}

	@Scheduled(cron = "0 0 * * * ?")
	public void cleanupExpiredUrls() {
		repository.deleteAllByExpiryDateBefore(LocalDateTime.now());
	}
}