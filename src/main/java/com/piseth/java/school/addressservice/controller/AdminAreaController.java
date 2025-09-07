package com.piseth.java.school.addressservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.service.AdminAreaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin-areas")
@RequiredArgsConstructor
public class AdminAreaController {

	private final AdminAreaService service;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<AdminAreaResponse> create(@Valid @RequestBody AdminAreaCreateRequest request){
		return service.create(request);
	}
}
