package com.piseth.java.school.addressservice.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.web.upload.ParsedRow;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Parses an uploaded Excel file (XLSX/XLS) into a stream of {@link ParsedRow}.
 *
 * Teaching points:
 * - We read the whole file content as bytes (reactive DataBuffer) and then let Apache POI parse it.
 * - We run blocking POI work on {@code boundedElastic()} so we don't block reactive threads.
 * - We normalize headers to lowercase, so header matching is case-insensitive.
 * - We use {@link DataFormatter} to get cell values as displayed text (important for preserving
 *   what users see in Excel). Students should still format the "code" column as TEXT in Excel to
 *   avoid losing leading zeros.
 *
 * Expected headers (case-insensitive): code, level, parentCode, nameKh, nameEn.
 * Internally we match against lowercase keys: "code", "level", "parentcode", "namekh", "nameen".
 */
@Component
@RequiredArgsConstructor
public class ExcelAdminAreaParserImpl implements ExcelAdminAreaParser {

    /**
     * Headers we require. We lowercase the actual header cells at read-time so matching is
     * case-insensitive (e.g., "ParentCode" or "parentcode" are both accepted).
     */
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "code", "level", "parentcode", "namekh", "nameen"
    );

    /**
     * Entry point: parse the multipart file into a Flux of ParsedRow.
     * <p>
     * Flow:
     * <ol>
     *   <li>Join all data buffers into one (we need all bytes for Apache POI).</li>
     *   <li>Convert to byte[].</li>
     *   <li>Parse rows (on boundedElastic).</li>
     * </ol>
     */
    @Override
    public Flux<ParsedRow> parse(final FilePart file) {
        return DataBufferUtils.join(file.content())
            .flatMapMany(buf -> {
                try {
                    final byte[] bytes = toBytes(buf);
                    return parseBytes(bytes);
                } finally {
                    // Always release the buffer to avoid memory leaks.
                    DataBufferUtils.release(buf);
                }
            });
    }

    /**
     * Defer blocking POI parsing to a worker scheduler.
     */
    private Flux<ParsedRow> parseBytes(final byte[] bytes) {
        return Mono.fromCallable(() -> readRows(bytes))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable);
    }

    /**
     * Core Excel parsing with Apache POI. Reads the first sheet and converts each non-empty row
     * into a {@link ParsedRow}.
     *
     * @throws Exception if the workbook/sheet is invalid or headers are missing.
     */
    private List<ParsedRow> readRows(final byte[] bytes) throws Exception {
        final List<ParsedRow> out = new ArrayList<>();

        // Try-with-resources to ensure POI workbook is closed.
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            final Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file has no sheets");
            }

            // DataFormatter returns the formatted text as seen in Excel UI.
            // NOTE for students: If the "code" column is numeric and not formatted with leading zeros,
            // Excel may display '01' but store it differently. Best practice: set the entire "code"
            // column to TEXT in Excel to preserve leading zeros reliably.
            final DataFormatter fmt = new DataFormatter(Locale.ROOT);

            // Header row (row 0) defines the column indices.
            final Row header = sheet.getRow(0);
            if (header == null) {
                throw new IllegalArgumentException("Missing header row");
            }

            // Build a map of headerName(lowercase) -> columnIndex
            final Map<String, Integer> idx = headerIndex(header, fmt);

            // Validate presence of required headers (case-insensitive).
            if (!idx.keySet().containsAll(REQUIRED_HEADERS)) {
                // Message lists canonical names so users know what to include.
                throw new IllegalArgumentException("Header must include: code, level, parentCode, nameKh, nameEn");
            }

            // Iterate data rows (1..lastRow). Row numbers we store are 1-based (Excel-style).
            final int last = sheet.getLastRowNum();
            for (int r = 1; r <= last; r++) {
                final Row row = sheet.getRow(r);
                if (row == null) {
                    // Empty row: skip
                    continue;
                }

                // Read each field by header index. The helper returns trimmed strings or null.
                final String code = cell(row, idx.get("code"), fmt);
                final String levelRaw = cell(row, idx.get("level"), fmt);
                final String parent = cell(row, idx.get("parentcode"), fmt);
                final String nameKh = cell(row, idx.get("namekh"), fmt);
                final String nameEn = cell(row, idx.get("nameen"), fmt);

                // If an entire row is blank, skip it to be forgiving to users.
                if (isBlank(code) && isBlank(levelRaw) && isBlank(parent) && isBlank(nameKh) && isBlank(nameEn)) {
                    continue;
                }

                // Parse level into enum. If unrecognized, we throw a clear error.
                final AdminLevel level = parseLevel(levelRaw);

                // Save 1-based lineNumber (r + 1) to help users locate issues in Excel.
                out.add(new ParsedRow(r + 1, code, level, parent, nameKh, nameEn));
            }
        }

        return out;
    }

    /**
     * Build a case-insensitive header map from the first row.
     * We lowercase header names so "ParentCode", "parentCode", or "PARENTCODE" all map to "parentcode".
     */
    private Map<String, Integer> headerIndex(final Row header, final DataFormatter fmt) {
        final Map<String, Integer> idx = new HashMap<>();
        for (int c = 0; c < header.getLastCellNum(); c++) {
            final Cell cell = header.getCell(c);
            final String raw = cell != null ? fmt.formatCellValue(cell) : null;
            final String key = raw != null ? raw.trim().toLowerCase(Locale.ROOT) : "";
            if (!key.isBlank()) {
                idx.put(key, c);
            }
        }
        return idx;
    }

    /**
     * Read a single cell by index and return its trimmed string value (or null).
     * Using {@link DataFormatter} keeps consistency with what users see in Excel.
     */
    private String cell(final Row row, final Integer col, final DataFormatter fmt) {
        if (row == null || col == null) {
            return null;
        }
        final Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        final String v = fmt.formatCellValue(cell);
        if (v == null) {
            return null;
        }
        final String t = v.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * Null-/empty-safe check for strings.
     */
    private boolean isBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Convert text from the "level" column to an {@link AdminLevel} enum.
     * Valid values are the enum names (e.g., PROVINCE, DISTRICT, COMMUNE, VILLAGE), case-insensitive.
     */
    private AdminLevel parseLevel(final String raw) {
        if (raw == null) {
            return null; // We'll validate null level later in validator/service
        }
        final String u = raw.trim().toUpperCase(Locale.ROOT);
        for (AdminLevel lvl : AdminLevel.values()) {
            if (lvl.name().equals(u)) {
                return lvl;
            }
        }
        // Clear message for users when level text is wrong.
        throw new IllegalArgumentException("Unknown level: " + raw);
    }

    /**
     * Copy bytes out of the reactive {@link DataBuffer}. Important: we must release the buffer
     * after copying (see caller) to avoid leaks.
     */
    private byte[] toBytes(final DataBuffer buf) {
        final byte[] bytes = new byte[buf.readableByteCount()];
        buf.read(bytes);
        return bytes;
    }
}
