package com.github.russp.jtorrt.util;

public final class StringUtils {

	private StringUtils() {
	}

	public static String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
