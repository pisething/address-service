package com.piseth.java.school.addressservice.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;

@Mapper(componentModel = "spring")
public interface AdminAreaMapper {
	
	@Mapping(target = "path", expression = "java(buildPath(dto.getCode()))")
	@Mapping(target = "createAt", ignore = true)
	@Mapping(target = "updateAt", ignore = true)
	@Mapping(target = "version", ignore = true)
	AdminArea toEntity(AdminAreaCreateRequest dto);
	
	
	default List<String> buildPath(String code){
		if( code == null || code.isBlank()) {
			return List.of();
		}
		String[] parts = code.split("-");
		List<String> path = new ArrayList<>(parts.length);
		String acc = "";
		for(int i = 0; i < parts.length; i++) {
			if(i == 0) {
				acc = parts[i];
			}else {
				acc = acc + "-" + parts[i];
			}
			path.add(acc);
		}
		return path;
	}
	
	AdminAreaResponse toResponse(AdminArea entity);
	
	@Mapping(target = "createAt", ignore = true)
	@Mapping(target = "updateAt", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "level", ignore = true)
	@Mapping(target = "code", ignore = true)
	@Mapping(target = "parentCode", ignore = true)
	@Mapping(target = "path", ignore = true)
	void update(@MappingTarget AdminArea target, AdminAreaUpdateRequest dto);

}
