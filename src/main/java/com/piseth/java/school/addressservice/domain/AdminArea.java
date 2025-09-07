package com.piseth.java.school.addressservice.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("adminAreas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminArea {
	@Id
	private String code;
	
	private AdminLevel level;
	private String parentCode;
	private String nameKh;
	private String nameEn;
	private List<String> path;
	private Instant createAt;
	private Instant updateAt;
	
	@Version
	private Long version;
}
