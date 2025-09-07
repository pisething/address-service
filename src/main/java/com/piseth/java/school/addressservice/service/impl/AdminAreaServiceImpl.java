package com.piseth.java.school.addressservice.service.impl;

import java.time.Instant;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;
import com.piseth.java.school.addressservice.mapper.AdminAreaMapper;
import com.piseth.java.school.addressservice.repository.AdminAreaRepository;
import com.piseth.java.school.addressservice.service.AdminAreaService;
import com.piseth.java.school.addressservice.validator.AdminAreaValidator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdminAreaServiceImpl implements AdminAreaService{
	
	private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "code");
	
	private final AdminAreaRepository repository;
	private final AdminAreaValidator validator;
	private final AdminAreaMapper mapper;
	
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
					return Mono.error(new IllegalStateException("Parent not found : " + candidate.getParentCode()));
				}
			});
		
	}
	
	// ensureCodeIsUnique
	private Mono<Void> ensureCodeIsUnique(final AdminArea candidate){
		return repository.existsById(candidate.getCode())
		.flatMap(exists ->{
			if(exists) {
				return Mono.error(new IllegalStateException("AdminArea already exists : " + candidate.getCode()));
			}else {
				return Mono.empty();
			}
		});
		
	}
	
	
	
	

	@Override
	public Mono<AdminAreaResponse> get(final String code) {
		return repository.findById(code).switchIfEmpty(Mono.error(new IllegalStateException("AdminArea not found: " + code)))
				.map(mapper::toResponse);
	}

	@Override
	public Flux<AdminAreaResponse> list(final AdminLevel level, final String parentCode) {
		if (level == null && (parentCode == null || parentCode.isBlank())) {
			// whole collection with sort
			return repository.findAll(DEFAULT_SORT).map(mapper::toResponse);
		}
		if (level != null && (parentCode == null || parentCode.isBlank())) {
			return repository.findByLevel(level, DEFAULT_SORT).map(mapper::toResponse);
		}
		if (level == null) {
			return repository.findByParentCode(parentCode, DEFAULT_SORT).map(mapper::toResponse);
		}
		return repository.findByLevelAndParentCode(level, parentCode, DEFAULT_SORT).map(mapper::toResponse);
	}

	@Override
	public Mono<Void> delete(final String code) {
	    return repository.existsById(code)
	        .flatMap(exists -> {
	            if (!exists) {
	                return Mono.error(new IllegalStateException("AdminArea not found: " + code));
	            }
	            return repository.existsByParentCode(code)
	                .flatMap(hasChildren -> hasChildren
	                    ? Mono.error(new IllegalStateException("Cannot delete: children exist for " + code))
	                    : repository.deleteById(code));
	        });
	}



	@Override
	public Mono<AdminAreaResponse> update(final String code, final AdminAreaUpdateRequest req) {
		final Instant now = Instant.now();
		return repository.findById(code).switchIfEmpty(Mono.error(new IllegalStateException("AdminArea not found: " + code)))
				.flatMap(entity -> {
					mapper.update(entity, req); // set names
					//entity.setUpdatedAt(now); // auditing can replace this
					return repository.save(entity);
				}).map(mapper::toResponse);
	}


	

}
