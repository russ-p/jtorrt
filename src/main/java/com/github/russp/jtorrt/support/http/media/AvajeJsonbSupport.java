package com.github.russp.jtorrt.support.http.media;

import io.avaje.jsonb.Jsonb;
import io.helidon.common.GenericType;
import io.helidon.common.config.Config;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Headers;
import io.helidon.http.HttpMediaType;
import io.helidon.http.WritableHeaders;
import io.helidon.http.media.EntityReader;
import io.helidon.http.media.EntityWriter;
import io.helidon.http.media.MediaSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Objects;

import static io.helidon.http.HeaderValues.CONTENT_TYPE_JSON;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AvajeJsonbSupport implements MediaSupport {

	private static final Logger log = LoggerFactory.getLogger(AvajeJsonbSupport.class);
	private final Jsonb jsonb;
	private final AvajeJsonbReader reader;
	private final AvajeJsonbWriter writer;

	private final String name;

	public static MediaSupport create(Config config, String name) {
		Objects.requireNonNull(config);
		Objects.requireNonNull(name);

		var deserializeConfig = config.get("deserialize");
		var serializeConfig = config.get("serializeConfig");
		var jsonb = Jsonb.builder()
				.failOnUnknown(deserializeConfig.get("fail-on-unknown").asBoolean().orElse(false))
				.mathTypesAsString(serializeConfig.get("math-types-as-string").asBoolean().orElse(false))
				.serializeEmpty(serializeConfig.get("empty").asBoolean().orElse(true))
				.serializeNulls(serializeConfig.get("nulls").asBoolean().orElse(false))
				.build();
		return new AvajeJsonbSupport(jsonb, new AvajeJsonbReader(jsonb), new AvajeJsonbWriter(jsonb), name);
	}

	private AvajeJsonbSupport(Jsonb jsonb, AvajeJsonbReader reader, AvajeJsonbWriter writer, String name) {
		this.jsonb = jsonb;
		this.reader = reader;
		this.writer = writer;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String type() {
		return "avaje-jsonb";
	}

	@Override
	public <T> ReaderResponse<T> reader(GenericType<T> type, Headers headers) {
		if (headers.contentType()
				.map(it -> it.test(MediaTypes.APPLICATION_JSON))
				.orElse(true)) {
			if (hasAdapter(type.type())) {
				return new ReaderResponse<>(SupportLevel.COMPATIBLE, this::reader);
			}
		}

		return ReaderResponse.unsupported();
	}

	@Override
	public <T> WriterResponse<T> writer(GenericType<T> type, Headers requestHeaders, WritableHeaders<?> responseHeaders) {
		// check if accepted
		for (HttpMediaType acceptedType : requestHeaders.acceptedTypes()) {
			if (acceptedType.test(MediaTypes.APPLICATION_JSON)) {
				if (hasAdapter(type.rawType())) {
					return new WriterResponse<>(SupportLevel.COMPATIBLE, this::writer);
				}
				return WriterResponse.unsupported();
			}
		}

		if (requestHeaders.acceptedTypes().isEmpty()) {
			if (hasAdapter(type.rawType())) {
				return new WriterResponse<>(SupportLevel.COMPATIBLE, this::writer);
			}
		}

		return WriterResponse.unsupported();
	}

	@Override
	public <T> ReaderResponse<T> reader(GenericType<T> type, Headers requestHeaders, Headers responseHeaders) {
		for (HttpMediaType acceptedType : requestHeaders.acceptedTypes()) {
			if (acceptedType.test(MediaTypes.APPLICATION_JSON) || acceptedType.mediaType().isWildcardType()) {
				Type type1 = type.type();
				if (hasAdapter(type1)) {
					return new ReaderResponse<>(SupportLevel.COMPATIBLE, this::reader);
				}
			}
		}

		if (requestHeaders.acceptedTypes().isEmpty()) {
			Type type1 = type.type();
			if (hasAdapter(type1)) {
				return new ReaderResponse<>(SupportLevel.COMPATIBLE, this::reader);
			}
		}

		return ReaderResponse.unsupported();
	}

	@Override
	public <T> WriterResponse<T> writer(GenericType<T> type, WritableHeaders<?> requestHeaders) {
		if (requestHeaders.contains(HeaderNames.CONTENT_TYPE)) {
			if (requestHeaders.contains(CONTENT_TYPE_JSON)) {
				if (hasAdapter(type.type())) {
					return new WriterResponse<>(SupportLevel.COMPATIBLE, this::writer);
				}
				return WriterResponse.unsupported();
			}
		} else {
			if (hasAdapter(type.type())) {
				return new WriterResponse<>(SupportLevel.SUPPORTED, this::writer);
			}
			return WriterResponse.unsupported();
		}
		return WriterResponse.unsupported();
	}

	<T> EntityReader<T> reader() {
		return reader;
	}

	<T> EntityWriter<T> writer() {
		return writer;
	}

	private boolean hasAdapter(Type type) {
		try {
			jsonb.adapter(type);
			return true;
		} catch (IllegalArgumentException e) {
			log.error("Failed to adapt type {}", type, e);
			return false;
		}
	}

}
