package com.piseth.java.school.addressservice.dto;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAreaSlimResponse {

	private String code;
	private AdminLevel level;
	private String parentCode;
	private String nameEn;
}
