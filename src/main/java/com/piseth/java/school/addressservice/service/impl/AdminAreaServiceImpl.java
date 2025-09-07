package com.piseth.java.school.addressservice.service.impl;

import org.springframework.stereotype.Service;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;
import com.piseth.java.school.addressservice.mapper.AdminAreaMapper;
import com.piseth.java.school.addressservice.repository.AdminAreaRepsitory;
import com.piseth.java.school.addressservice.service.AdminAreaService;
import com.piseth.java.school.addressservice.validator.AdminAreaValidator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdminAreaServiceImpl implements AdminAreaService{
	
	private final AdminAreaRepsitory repository;
	private final AdminAreaValidator validator;
	private final AdminAreaMapper mapper;

	/*
	 map from dto to entity
	 basic validate 
	 => checkParentCodeExists
	 => ensureCodeIsUnique
	 => save
	 .. map to response
	 
	 
	 
	 * 
	 * */
	
	@Override
	public Mono<AdminAreaResponse> create(AdminAreaCreateRequest dto) {
		return Mono.fromCallable(() -> {
			final AdminArea candidate = mapper.toEntity(dto);
			validator.validate(candidate);
			return candidate;
		}).flatMap(candidate ->{
			return Mono.when(checkParentCodeExists(candidate), ensureCodeIsUnique(candidate))
			.thenReturn(candidate)
			.flatMap(c -> repository.save(c))
			.map(mapper::toResponse)
			;
		});
	}
	
	// checkParentCodeExists
	
	private Mono<Void> checkParentCodeExists(final AdminArea candidate){
		if(candidate.getLevel() == AdminLevel.PROVINCE) {
			return Mono.empty();
		}
		
		return repository.existsById(candidate.getParentCode())
			.flatMap(exists ->{
				if(exists) {
					return Mono.empty();
				}else {
					return Mono.error(new IllegalArgumentException("Parent not found : " + candidate.getParentCode()));
				}
			});
		
	}
	
	// ensureCodeIsUnique
	private Mono<Void> ensureCodeIsUnique(final AdminArea candidate){
		return repository.existsById(candidate.getCode())
		.flatMap(exists ->{
			if(exists) {
				return Mono.error(new IllegalArgumentException("AdminArea already exists: : " + candidate.getCode()));
			}else {
				return Mono.empty();
			}
		});
		
	}
	
	
	
	

	@Override
	public Mono<AdminAreaResponse> get(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Void> delete(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<AdminAreaResponse> update(AdminAreaUpdateRequest dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<AdminAreaResponse> list(AdminLevel level, String parentCode) {
		// TODO Auto-generated method stub
		return null;
	}

}
