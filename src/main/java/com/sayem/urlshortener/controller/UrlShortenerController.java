package com.sayem.urlshortener.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sayem.urlshortener.service.UrlShortenerService;

@RestController
@RequestMapping("/api")
public class UrlShortenerController {
	private final UrlShortenerService service;

	UrlShortenerController(UrlShortenerService service) {
		this.service = service;
	}

	@PostMapping("/shorten")
	public ResponseEntity<String> shorten(@RequestBody String url) {
		return ResponseEntity.ok(service.shortenUrl(url));
	}
}