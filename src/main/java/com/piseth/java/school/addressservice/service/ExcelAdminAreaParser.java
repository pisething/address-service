package com.piseth.java.school.addressservice.service;

import org.springframework.http.codec.multipart.FilePart;

import com.piseth.java.school.addressservice.dto.ParsedRow;

import reactor.core.publisher.Flux;

public interface ExcelAdminAreaParser {
	
	Flux<ParsedRow> parse(FilePart file);

}
