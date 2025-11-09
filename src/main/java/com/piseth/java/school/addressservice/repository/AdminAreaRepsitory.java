package com.piseth.java.school.addressservice.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminAreaRepsitory extends ReactiveMongoRepository<AdminArea, String>{
	
	Flux<AdminArea> findByLevel(AdminLevel level, Sort sort);
	
	Flux<AdminArea> findByParentCode(String parentCode, Sort sort);
	
	Flux<AdminArea> findByLevelAndParentCode(AdminLevel level, String parentCode, Sort sort);
	
	Mono<Boolean> existsByParentCode(String parentCode);
	
	// projection
	@Query(value = "{'level': ?0}", 
			fields = "{'code':1, 'level':1, 'parentCode':1,'nameEn':1}")
	Flux<AdminArea> findSlimByLevel(AdminLevel level, Sort sort);
	
	@Query(value = "{'parentCode': ?0}", 
			fields = "{'code':1, 'level':1, 'parentCode':1,'nameEn':1}")
	Flux<AdminArea> findSlimByParentCode(String parentCode, Sort sort);
	
	@Query(value = "{'level': ?0, 'parentCode': ?1}", 
			fields = "{'code':1, 'level':1, 'parentCode':1,'nameEn':1}")
	Flux<AdminArea> findSlimByLevelAndParentCode(AdminLevel level, String parentCode, Sort sort);
	

	@Query(value = "{}", 
			fields = "{'code':1, 'level':1, 'parentCode':1,'nameEn':1}")
	Flux<AdminArea> findSlimAll(Sort sort);
}
