package com.sayem.urlshortener.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "url_mapping")
@Getter
@Setter
public class UrlMapping {
	@Id
	private String id;
	private String originalUrl;
	private LocalDateTime createdAt;
	private long clickCount;
	private LocalDateTime expiryDate;
}