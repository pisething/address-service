// src/main/java/com/piseth/java/school/addressservice/web/AdminAreaUploadController.java
package com.piseth.java.school.addressservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.piseth.java.school.addressservice.dto.UploadSummary;
import com.piseth.java.school.addressservice.service.AdminAreaImportService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin-areas")
@RequiredArgsConstructor
public class AdminAreaUploadController {

    private final AdminAreaImportService importService;

    @PostMapping(value = "/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<UploadSummary> uploadExcel(@RequestPart("file") final FilePart file,
                                           @RequestParam(defaultValue = "false") final boolean dryRun) {
        return importService.importExcel(file, dryRun);
    }
}
