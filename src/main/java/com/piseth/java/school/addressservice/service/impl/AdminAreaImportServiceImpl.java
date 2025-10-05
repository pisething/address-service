package com.piseth.java.school.addressservice.service.impl;

import java.util.List;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.Outcome;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.ParsedRow;
import com.piseth.java.school.addressservice.dto.RowError;
import com.piseth.java.school.addressservice.dto.RowResult;
import com.piseth.java.school.addressservice.dto.UploadSummary;
import com.piseth.java.school.addressservice.mapper.AdminAreaMapper;
import com.piseth.java.school.addressservice.mapper.ParsedRowMapper;
import com.piseth.java.school.addressservice.mapper.UploadSummaryMapper;
import com.piseth.java.school.addressservice.service.AdminAreaImportService;
import com.piseth.java.school.addressservice.service.AdminAreaService;
import com.piseth.java.school.addressservice.service.ExcelAdminAreaParser;
import com.piseth.java.school.addressservice.service.helper.ImportAccumulator;
import com.piseth.java.school.addressservice.service.helper.RowErrorClassifier;
import com.piseth.java.school.addressservice.validator.AdminAreaValidator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdminAreaImportServiceImpl implements AdminAreaImportService{

	private final ExcelAdminAreaParser parser;
	private final ParsedRowMapper parsedRowMapper;
	private final AdminAreaMapper adminAreaMapper;
	private final AdminAreaValidator validator;
	private final AdminAreaService adminAreaService;
	private final RowErrorClassifier rowErrorClassifier;
	private final UploadSummaryMapper uploadSummaryMapper;
	
	@Override
	public Mono<UploadSummary> importExcel(FilePart file, boolean dryRun) {
		
		
		
		return parser.parse(file)
			.sort(ParsedRow.BY_DEPTH)
			.concatMap(row -> handleRow(row, dryRun))
			.reduce(new ImportAccumulator(), ImportAccumulator::accumulate)
			.map(uploadSummaryMapper::toUploadSummary);
			
	}
	
	private Mono<RowResult> handleRow(final ParsedRow row,boolean dryRun){
		AdminAreaCreateRequest createRequest = parsedRowMapper.toCreateRequest(row);
		
		return validate(createRequest)
		.then(maybeCreate(createRequest, dryRun))
		.map(ok -> RowResult.inserted())
		.onErrorResume(ex ->{
			final Outcome outcome = rowErrorClassifier.classify(ex);
			final String msg = rowErrorClassifier.safeMessage(ex);
			RowError error = new RowError(row.lineNumber(), row.code(), msg);
			return Mono.just(RowResult.error(outcome, error));
		});
		
	}
	
	private Mono<Boolean> maybeCreate(final AdminAreaCreateRequest req, boolean dryRun){
		if(dryRun) {
			return Mono.just(Boolean.TRUE);
		}
		return adminAreaService.create(req).thenReturn(Boolean.TRUE);
	}
	
	private Mono<Void> validate(final AdminAreaCreateRequest req){
		return Mono.fromRunnable(() ->{
			AdminArea adminArea = adminAreaMapper.toEntity(req);
			validator.validate(adminArea);
		});
	}
	
}
