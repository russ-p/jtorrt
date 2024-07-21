package com.github.russp.jtorrt;

import com.github.russp.jtorrt.common.Storage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemStorage implements Storage {

	private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

	@Override
	public void put(String key, String value) {
		if (value == null) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}

	@Override
	public Optional<String> get(String key) {
		return Optional.ofNullable(map.get(key));
	}
}
