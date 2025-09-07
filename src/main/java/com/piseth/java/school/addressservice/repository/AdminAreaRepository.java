package com.piseth.java.school.addressservice.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminAreaRepository extends ReactiveMongoRepository<AdminArea, String>{
	
	Flux<AdminArea> findByLevel(AdminLevel level, Sort sort);
	
	Flux<AdminArea> findByParentCode(String parentCode, Sort sort);
	
	Flux<AdminArea> findByLevelAndParentCode(AdminLevel level, String parentCode, Sort sort);
	
	Mono<Boolean> existsByParentCode(String parentCode);

}
