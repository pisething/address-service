package com.piseth.java.school.addressservice.util;

import org.springframework.http.codec.multipart.FilePart;

import com.piseth.java.school.addressservice.web.upload.ParsedRow;

import reactor.core.publisher.Flux;

public interface ExcelAdminAreaParser {
    Flux<ParsedRow> parse(FilePart file);
}
