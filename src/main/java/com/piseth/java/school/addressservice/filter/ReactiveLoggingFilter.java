package com.piseth.java.school.addressservice.filter;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
//@Component
public class ReactiveLoggingFilter implements WebFilter{

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// GET /api/rooms - 200 (81 ms)
		
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		
		Instant start = Instant.now();
		String method = request.getMethod() != null? request.getMethod().name() : "UNKNOWN";
		String path = request.getURI().getPath();
		
		log.info("➡️ {} {}", method, path);
		
		return chain.filter(exchange)
				.doOnSuccess(done ->{
					Instant end = Instant.now();
					Duration duration = Duration.between(start, end);
					int status = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
					log.info("⬅️ {} {} - {} ({} ms)", method, path, status, duration.toMillis());
				})
				;
	}

}
