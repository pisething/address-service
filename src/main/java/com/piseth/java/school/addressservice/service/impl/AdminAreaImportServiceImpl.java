package com.piseth.java.school.addressservice.service.impl;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.RowError;
import com.piseth.java.school.addressservice.dto.RowResult;
import com.piseth.java.school.addressservice.dto.UploadSummary;
import com.piseth.java.school.addressservice.mapper.AdminAreaImportMapper;
import com.piseth.java.school.addressservice.mapper.AdminAreaMapper;
import com.piseth.java.school.addressservice.mapper.ParsedRowMapper;
import com.piseth.java.school.addressservice.service.AdminAreaImportService;
import com.piseth.java.school.addressservice.service.AdminAreaService;
import com.piseth.java.school.addressservice.util.ExcelAdminAreaParser;
import com.piseth.java.school.addressservice.util.ImportAccumulator;
import com.piseth.java.school.addressservice.util.RowErrorClassifier;
import com.piseth.java.school.addressservice.validator.AdminAreaValidator;
import com.piseth.java.school.addressservice.web.upload.ParsedRow;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdminAreaImportServiceImpl implements AdminAreaImportService {

	private final ExcelAdminAreaParser parser;
	private final ParsedRowMapper parsedRowMapper; // ParsedRow -> AdminAreaCreateRequest (MapStruct)
	private final AdminAreaService adminAreaService; // create(AdminAreaCreateRequest)
	private final AdminAreaValidator validator; // validates ENTITY
	private final AdminAreaMapper adminAreaMapper; // DTO -> ENTITY for validator
	private final RowErrorClassifier errorClassifier; // message + outcome
	private final AdminAreaImportMapper importMapper; // counters + errors -> summary

	@Override
	public Mono<UploadSummary> importExcel(final FilePart file, final boolean dryRun) {
		return parser.parse(file).sort(ParsedRow.BY_DEPTH).concatMap(row -> handleRow(row, dryRun))
				.reduce(new ImportAccumulator(), ImportAccumulator::accumulate)
				.map(acc -> acc.toSummary(importMapper));
	}

	private reactor.core.publisher.Mono<RowResult> handleRow(final ParsedRow row, final boolean dryRun) {
		final AdminAreaCreateRequest req = parsedRowMapper.toCreateRequest(row);

		return validate(req).then(maybeCreate(req, dryRun)).map(ok -> RowResult.inserted()).onErrorResume(ex -> {
			final String msg = errorClassifier.safeMessage(ex);
			final Outcome outcome = errorClassifier.classify(msg);
			final RowError err = new RowError(row.lineNumber(), row.code(), msg);
			return reactor.core.publisher.Mono.just(RowResult.error(outcome, err));
		});
	}

	private reactor.core.publisher.Mono<Void> validate(final AdminAreaCreateRequest req) {
		return reactor.core.publisher.Mono.fromRunnable(() -> {
			// validator expects an entity — use MapStruct to map the DTO
			var entity = adminAreaMapper.toEntity(req);
			validator.validate(entity);
		});
	}

	private reactor.core.publisher.Mono<Boolean> maybeCreate(final AdminAreaCreateRequest req, final boolean dryRun) {
		if (dryRun) {
			return reactor.core.publisher.Mono.just(Boolean.TRUE);
		} else {
			return adminAreaService.create(req).thenReturn(Boolean.TRUE);
		}
	}
}
