package com.piseth.java.school.addressservice.dto;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAreaSlimResponse {
    private String code;
    private AdminLevel level;
    private String parentCode;
    private String nameKh;
    private String nameEn;
}
