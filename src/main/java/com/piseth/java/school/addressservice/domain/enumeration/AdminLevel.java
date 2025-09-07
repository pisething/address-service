package com.piseth.java.school.addressservice.domain.enumeration;

import lombok.Getter;

@Getter
public enum AdminLevel {
	PROVINCE, DISTRICT, COMMUNE, VILLAGE;

	public int depth() {
		return switch (this) {
		case PROVINCE -> 1;
		case DISTRICT -> 2;
		case COMMUNE -> 3;
		case VILLAGE -> 4;
		};
	}
}
