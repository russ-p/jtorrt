package com.github.russp.jtorrt.app;

import com.github.russp.jtorrt.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class ComponentRegistry<PROTOTYPE> {

	private final Map<Class<?>, Function<PROTOTYPE, ?>> classes = new HashMap<>();
	private final Map<String, Function<PROTOTYPE, ?>> names = new HashMap<>();

	public <T> void register(Class<T> clazz, String name, Function<PROTOTYPE, ?> provider) {
		classes.put(clazz, provider);
		names.put(name, provider);
	}

	public <T> void singleton(Class<T> clazz, Function<PROTOTYPE, ?> provider) {
		register(clazz, clazz.getSimpleName().transform(StringUtils::capitalize), new ComponentRegistry.Singleton<>(provider));
	}

	public <T> Function<PROTOTYPE, T> get(Class<T> clazz) {
		if (!classes.containsKey(clazz)) {
			throw new IllegalArgumentException("Class " + clazz + " does not registered");
		}
		return (Function<PROTOTYPE, T>) classes.get(clazz);
	}

	private static final class Singleton<C, T> implements Function<C, T> {

		private final Function<C, T> provider;
		private volatile T instance;

		private Singleton(Function<C, T> provider) {
			this.provider = provider;
		}

		@Override
		public T apply(C app) {
			if (instance == null) {
				synchronized (ComponentRegistry.Singleton.class) {
					if (instance == null) {
						instance = provider.apply(app);
					}
				}
			}
			return instance;
		}
	}
}
