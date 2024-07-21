package com.github.russp.jtorrt.common;

public record InfoHash(String value) {
	public InfoHash(String value) {
		this.value = value.toUpperCase();
	}
}
