package com.piseth.java.school.addressservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaSlimResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;
import com.piseth.java.school.addressservice.service.AdminAreaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
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
	
	@GetMapping("/{code}")
	public Mono<AdminAreaResponse> get(@PathVariable String code){
		return service.get(code);
	}
	
	@GetMapping
	public Flux<AdminAreaResponse> list(
			@RequestParam(required = false) AdminLevel level,
			@RequestParam(required = false) String parentCode
			
			){
		return service.list(level, parentCode);
	}
	
	@GetMapping("/slim")
	public Flux<AdminAreaSlimResponse> listSlim(
			@RequestParam(required = false) AdminLevel level,
			@RequestParam(required = false) String parentCode
			
			){
		return service.listSlim(level, parentCode);
	}
	
	@DeleteMapping("/{code}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void>  delete(@PathVariable String code){
		return service.delete(code);
	}
	
	@PutMapping("/{code}")
	@ResponseStatus(HttpStatus.OK)
	public Mono<AdminAreaResponse> update(@PathVariable String code, @Valid @RequestBody AdminAreaUpdateRequest request){
		return service.update(code, request);
	}
	
	
}
