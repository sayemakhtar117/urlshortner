package com.sayem.urlshortener.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sayem.urlshortener.model.UrlMapping;

public interface UrlMappingRepository extends MongoRepository<UrlMapping, String> {
	void deleteAllByExpiryDateBefore(LocalDateTime now);
}