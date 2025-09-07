package com.piseth.java.school.addressservice.dto;

import java.time.Instant;
import java.util.List;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAreaResponse {

	private String code;

	private AdminLevel level;
	private String parentCode;
	private String nameKh;
	private String nameEn;
	private List<String> path;
	private Instant createAt;
	private Instant updateAt;
}
