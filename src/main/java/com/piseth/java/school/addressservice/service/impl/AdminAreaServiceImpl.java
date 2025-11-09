package com.piseth.java.school.addressservice.service.impl;

import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaSlimResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;
import com.piseth.java.school.addressservice.exception.AdminAreaNotFoundException;
import com.piseth.java.school.addressservice.exception.ChildrenExistException;
import com.piseth.java.school.addressservice.exception.DuplicateAdminAreaException;
import com.piseth.java.school.addressservice.exception.ParentNotFoundException;
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
	
	private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "code");

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
					return Mono.error(new ParentNotFoundException(candidate.getParentCode()));
				}
			});
		
	}
	
	// ensureCodeIsUnique
	private Mono<Void> ensureCodeIsUnique(final AdminArea candidate){
		return repository.existsById(candidate.getCode())
		.flatMap(exists ->{
			if(exists) {
				return Mono.error(new DuplicateAdminAreaException(candidate.getCode()));
			}else {
				return Mono.empty();
			}
		});
		
	}
	
	
	
	

	@Override
	public Mono<AdminAreaResponse> get(String code) {
		return repository.findById(code)
				.switchIfEmpty(Mono.error(new AdminAreaNotFoundException(code)))
				.map(mapper::toResponse);
	}

	// delete :
	//1. exist
	//2. no children
	@Override
	public Mono<Void> delete(String code) {
		return repository.existsById(code)
			.flatMap(exists ->{
				if(!exists) {
					return Mono.error(new AdminAreaNotFoundException(code));
				}
				return repository.existsByParentCode(code)
					.flatMap(hasChildren ->{
						if(hasChildren) {
							return Mono.error(new ChildrenExistException(code));
						}
						return repository.deleteById(code);
					});
			});
			
	}

	@Override
	public Mono<AdminAreaResponse> update(String code, AdminAreaUpdateRequest dto) {
		return repository.findById(code)
			.switchIfEmpty(Mono.error(new AdminAreaNotFoundException(code)))
			.flatMap(entity ->{
				mapper.update(entity, dto);
				return repository.save(entity);
			})
			.map(mapper::toResponse);
	}

	@Override
	public Flux<AdminAreaResponse> list(AdminLevel level, String parentCode) {
		
		final boolean hasLevel = Objects.nonNull(level);
		final boolean hasParent = StringUtils.hasText(parentCode);
		
		if(hasLevel && hasParent) {
			return repository.findByLevelAndParentCode(level, parentCode, DEFAULT_SORT)
					.map(mapper::toResponse);
		}
		
		if(hasLevel) {
			return repository.findByLevel(level, DEFAULT_SORT).map(mapper::toResponse);
		}
		
		if(hasParent) {
			return repository.findByParentCode(parentCode, DEFAULT_SORT).map(mapper::toResponse);
		}
		
		return repository.findAll(DEFAULT_SORT).map(mapper::toResponse);
	}

	@Override
	public Flux<AdminAreaSlimResponse> listSlim(AdminLevel level, String parentCode) {
		final boolean hasLevel = Objects.nonNull(level);
		final boolean hasParent = StringUtils.hasText(parentCode);
		
		if(hasLevel && hasParent) {
			return repository.findSlimByLevelAndParentCode(level, parentCode, DEFAULT_SORT)
					.map(mapper::toSlimResponse);
		}
		
		if(hasLevel) {
			return repository.findSlimByLevel(level, DEFAULT_SORT).map(mapper::toSlimResponse);
		}
		
		if(hasParent) {
			return repository.findSlimByParentCode(parentCode, DEFAULT_SORT).map(mapper::toSlimResponse);
		}
		
		return repository.findSlimAll(DEFAULT_SORT).map(mapper::toSlimResponse);
	}

}
