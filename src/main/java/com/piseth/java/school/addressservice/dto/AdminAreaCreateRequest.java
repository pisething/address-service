package com.piseth.java.school.addressservice.dto;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAreaCreateRequest {

	@NotBlank
	@Pattern(regexp = "^[0-9]{2}(?:-[0-9]{2}){0,3}$", message = "code must look like 12 or 12-03-07-02")
	private String code;
	
	@NotNull
	private AdminLevel level;
	
	private String parentCode;
	
	@NotBlank
	private String nameKh;
	
	@NotBlank
	private String nameEn;
}
