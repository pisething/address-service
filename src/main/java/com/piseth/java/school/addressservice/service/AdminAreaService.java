package com.piseth.java.school.addressservice.service;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaSlimResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminAreaService {
	Mono<AdminAreaResponse> create(AdminAreaCreateRequest dto);
	Mono<AdminAreaResponse> get(String code);
	Mono<Void> delete(String code);
	Mono<AdminAreaResponse> update(String code, AdminAreaUpdateRequest dto);
	Flux<AdminAreaResponse> list(AdminLevel level, String parentCode);
	Flux<AdminAreaSlimResponse> listSlim(AdminLevel level, String parentCode);
}
