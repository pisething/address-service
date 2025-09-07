package com.piseth.java.school.addressservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAreaUpdateRequest {
	
	@NotBlank
	private String nameKh;
	
	@NotBlank
	private String nameEn;
}
