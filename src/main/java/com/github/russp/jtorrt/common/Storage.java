package com.github.russp.jtorrt.common;

import java.util.Optional;

public interface Storage {

	void put(String key, String value);

	Optional<String> get(String key);

}
