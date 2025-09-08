package com.piseth.java.school.addressservice.service;

import org.springframework.http.codec.multipart.FilePart;

import com.piseth.java.school.addressservice.dto.UploadSummary;

import reactor.core.publisher.Mono;

public interface AdminAreaImportService {
    Mono<UploadSummary> importExcel(FilePart file, boolean dryRun);
}
