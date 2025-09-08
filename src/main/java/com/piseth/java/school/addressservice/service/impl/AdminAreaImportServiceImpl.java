package com.piseth.java.school.addressservice.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.ImportCounters;
import com.piseth.java.school.addressservice.dto.RowError;
import com.piseth.java.school.addressservice.dto.UploadSummary;
import com.piseth.java.school.addressservice.mapper.AdminAreaImportMapper;
import com.piseth.java.school.addressservice.mapper.AdminAreaMapper;
import com.piseth.java.school.addressservice.service.AdminAreaImportService;
import com.piseth.java.school.addressservice.service.AdminAreaService;
import com.piseth.java.school.addressservice.util.ExcelAdminAreaParser;
import com.piseth.java.school.addressservice.validator.AdminAreaValidator;
import com.piseth.java.school.addressservice.web.upload.ParsedRow;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdminAreaImportServiceImpl implements AdminAreaImportService {

    private final ExcelAdminAreaParser parser;
    private final AdminAreaService adminAreaService;
    private final AdminAreaValidator validator;
    private final AdminAreaImportMapper importMapper;
    private final AdminAreaMapper adminAreaMapper;

    @Override
    public Mono<UploadSummary> importExcel(final FilePart file, final boolean dryRun) {
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger inserted = new AtomicInteger(0);
        final AtomicInteger duplicates = new AtomicInteger(0);
        final AtomicInteger validationErrors = new AtomicInteger(0);
        final AtomicInteger parentMissing = new AtomicInteger(0);
        final AtomicInteger otherErrors = new AtomicInteger(0);
        final List<RowError> errors = Collections.synchronizedList(new ArrayList<>());

        return parser.parse(file)
            .sort(depthComparator())
            .concatMap(row -> processRow(
                row, dryRun, total, inserted, duplicates, validationErrors, parentMissing, otherErrors, errors
            ))
            .then(Mono.fromSupplier(() -> buildSummary(total, inserted, duplicates, validationErrors, parentMissing, otherErrors, errors)));
    }

    // ---------- pipeline steps ----------

    private Mono<Boolean> processRow(
        final ParsedRow row,
        final boolean dryRun,
        final AtomicInteger total,
        final AtomicInteger inserted,
        final AtomicInteger duplicates,
        final AtomicInteger validationErrors,
        final AtomicInteger parentMissing,
        final AtomicInteger otherErrors,
        final List<RowError> errors
    ) {
        final AdminAreaCreateRequest req = toRequest(row);

        return validate(req)
            .then(maybeCreate(req, dryRun))
            .doOnNext(ok -> incrementTotals(ok, total, inserted))
            .onErrorResume(ex -> onRowError(ex, row, total, duplicates, validationErrors, parentMissing, otherErrors, errors));
    }

    private Mono<Void> validate(final AdminAreaCreateRequest req) {
        return Mono.fromRunnable(() -> {
        	AdminArea entity = adminAreaMapper.toEntity(req);
            validator.validate(entity);
        });
    }

    private Mono<Boolean> maybeCreate(final AdminAreaCreateRequest req, final boolean dryRun) {
        if (dryRun) {
            return Mono.just(Boolean.TRUE);
        } else {
            return adminAreaService.create(req).thenReturn(Boolean.TRUE);
        }
    }

    // ---------- helpers ----------

    private void incrementTotals(final Boolean ok,
                                 final AtomicInteger total,
                                 final AtomicInteger inserted) {
        total.incrementAndGet();
        if (Boolean.TRUE.equals(ok)) {
            inserted.incrementAndGet();
        }
    }

    private Mono<Boolean> onRowError(
        final Throwable ex,
        final ParsedRow row,
        final AtomicInteger total,
        final AtomicInteger duplicates,
        final AtomicInteger validationErrors,
        final AtomicInteger parentMissing,
        final AtomicInteger otherErrors,
        final List<RowError> errors
    ) {
        total.incrementAndGet();

        final String msg = safeMessage(ex);

        if (isDuplicate(msg)) {
            duplicates.incrementAndGet();
        } else if (isParentMissing(msg)) {
            parentMissing.incrementAndGet();
        } else if (isValidation(msg)) {
            validationErrors.incrementAndGet();
        } else {
            otherErrors.incrementAndGet();
        }

        errors.add(new RowError(row.lineNumber(), row.code(), msg));
        return Mono.empty();
    }

    private String safeMessage(final Throwable ex) {
        final String m = ex.getMessage();
        if (m != null) {
            return m;
        } else {
            return ex.getClass().getSimpleName();
        }
    }

    private boolean isDuplicate(final String msg) {
        return msg.startsWith("AdminArea already exists");
    }

    private boolean isParentMissing(final String msg) {
        return msg.startsWith("Parent not found");
    }

    private boolean isValidation(final String msg) {
        final String lower = msg.toLowerCase(Locale.ROOT);
        if (lower.contains("required")) {
            return true;
        } else if (lower.contains("depth")) {
            return true;
        } else if (lower.contains("look like")) {
            return true;
        } else if (lower.contains("unknown level")) {
            return true;
        } else {
            return false;
        }
    }

    private UploadSummary buildSummary(
        final AtomicInteger total,
        final AtomicInteger inserted,
        final AtomicInteger duplicates,
        final AtomicInteger validationErrors,
        final AtomicInteger parentMissing,
        final AtomicInteger otherErrors,
        final List<RowError> errors
    ) {
        final ImportCounters counters = new ImportCounters(
            total.get(),
            inserted.get(),
            duplicates.get(),
            validationErrors.get(),
            parentMissing.get(),
            otherErrors.get()
        );
        return importMapper.toSummary(counters, errors);
    }

    private Comparator<ParsedRow> depthComparator() {
        return Comparator.comparingInt(ParsedRow::depth);
    }

    private AdminAreaCreateRequest toRequest(final ParsedRow row) {
        final AdminAreaCreateRequest req = new AdminAreaCreateRequest();
        req.setCode(row.code());
        req.setLevel(row.level());
        req.setParentCode(row.parentCode());
        req.setNameKh(row.nameKh());
        req.setNameEn(row.nameEn());
        return req;
    }
}
