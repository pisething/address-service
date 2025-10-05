package com.piseth.java.school.addressservice.service.impl;

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
import com.piseth.java.school.addressservice.dto.ParsedRow;
import com.piseth.java.school.addressservice.service.ExcelAdminAreaParser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 *  -We read the whole file content as bytes and then let Apache POI parse it
 *  -We run blocking POI work on (boundedElastic)
 *  -We normalize header to lowercase
 *  -We use DataFormatter to get cell values as displayed text
 * 
 * 
 * 
 * 
 * 
 * */


@Component
public class ExcelAdminAreaParserImpl implements ExcelAdminAreaParser{
	
	private static final Set<String> REQUIRED_HAEDERS = Set.of(
			"code","level","parentcode","namekh","nameen"
			);

	@Override
	public Flux<ParsedRow> parse(FilePart file) {
		return DataBufferUtils.join(file.content())
			.flatMapMany(buf ->{
				try {
					final byte[] bytes = toBytes(buf);
					return parseBytes(bytes);
				} finally {
					DataBufferUtils.release(buf);
				}
				
			});
	}
	
	private byte[] toBytes(final DataBuffer buf) {
		final byte[] bytes = new byte[buf.readableByteCount()];
		buf.read(bytes);
		return bytes;
	}
	
	private Flux<ParsedRow> parseBytes(final byte[] bytes){
		return Mono.fromCallable(() -> readRows(bytes))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMapMany(Flux::fromIterable);
	}
	
	private List<ParsedRow> readRows(final byte[] bytes) throws Exception{
		
		final List<ParsedRow> out = new ArrayList<>();
		
		try(Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))){
			final Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
			if(sheet == null) {
				throw new IllegalArgumentException("Excel file has no sheet!");
			}
			
			final DataFormatter fmt = new DataFormatter(Locale.ROOT);
			
			final Row header = sheet.getRow(0);
			
			if(header == null) {
				throw new IllegalArgumentException("Missing header row");
			}
			
			Map<String, Integer> idx = headerIndex(header, fmt);
			if(!idx.keySet().containsAll(REQUIRED_HAEDERS)) {
				throw new IllegalArgumentException("Header must include: code, level, parentCode, nameKh, nameEn");
			}
			
			final int last = sheet.getLastRowNum();
			for(int r = 1; r <= last; r++) {
				final Row row = sheet.getRow(r);
				if(row == null) {
					continue;
				}
				
				final String code = cell(row, idx.get("code"), fmt);
				final String level = cell(row, idx.get("level"), fmt);
				final String parent = cell(row, idx.get("parentcode"), fmt);
				final String nameKh = cell(row, idx.get("namekh"), fmt);
				final String nameEn = cell(row, idx.get("nameen"), fmt);
				
				if(isBlank(code) && 
						isBlank(level) && 
						isBlank(parent) && 
						isBlank(nameEn) && 
						isBlank(nameKh)) {
					continue;
				}
				
				final AdminLevel adminLevel = parseLevel(level);
				out.add(new ParsedRow(r +1, code, adminLevel, parent, nameKh, nameEn));
			}
		}
		
		return out;
	}
	
	private AdminLevel parseLevel(final String raw) {
		if(raw == null) {
			return null;
		}
		final String u = raw.trim().toUpperCase();
		for(AdminLevel lvl : AdminLevel.values()) {
			if(lvl.name().equals(u)) {
				return lvl;
			}
		}
		throw new IllegalArgumentException("Unknown level");
	}
	
	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
	
	private String cell(final Row row, final Integer col, final DataFormatter fmt) {
		if(row == null || col == null) {
			return null;
		}
		final Cell cell = row.getCell(col);
		if(cell == null) {
			return null;
		}
		final String v = fmt.formatCellValue(cell);
		if(v == null) {
			return null;
		}
		final String t = v.trim();
		if(t.isEmpty()) {
			return null;
		}
		return t;
	}
	
	private Map<String, Integer> headerIndex(final Row header, final DataFormatter fmt){
		final Map<String, Integer> idx = new HashMap<>();
		for(int c =0; c < header.getLastCellNum(); c++) {
			final Cell cell = header.getCell(c);
			final String raw = cell != null ? fmt.formatCellValue(cell) : null;
			final String key = raw != null? raw.trim().toLowerCase(Locale.ROOT) : "";
			if(!key.isBlank()) {
				idx.put(key, c);
			}
		}
		
		return idx;
	}

}
