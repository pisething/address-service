// src/main/java/com/piseth/java/school/addressservice/web/upload/ParsedRow.java
package com.piseth.java.school.addressservice.web.upload;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

public record ParsedRow(
    int lineNumber,
    String code,
    AdminLevel level,
    String parentCode,
    String nameKh,
    String nameEn
) {
    public int depth() {
        if (code == null || code.isBlank()) {
            return Integer.MAX_VALUE;
        } else {
            return code.split("-").length;
        }
    }
}
