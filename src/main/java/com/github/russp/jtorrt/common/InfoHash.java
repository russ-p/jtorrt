package com.github.russp.jtorrt.common;

import io.avaje.jsonb.Json;

@Json
public record InfoHash(@Json.Value String value) {
	public InfoHash(String value) {
		this.value = value.toUpperCase();
	}
}
