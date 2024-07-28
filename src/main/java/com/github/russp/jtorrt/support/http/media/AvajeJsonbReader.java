package com.github.russp.jtorrt.support.http.media;

import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.Jsonb;
import io.helidon.common.GenericType;
import io.helidon.http.Headers;
import io.helidon.http.HttpMediaType;
import io.helidon.http.media.EntityReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AvajeJsonbReader<T> implements EntityReader<T> {

	private final Jsonb jsonb;

	public AvajeJsonbReader(Jsonb jsonb) {
		this.jsonb = jsonb;
	}

	@Override
	public T read(GenericType<T> type, InputStream stream, Headers headers) {
		return read(type, stream, contentTypeCharset(headers));
	}

	@Override
	public T read(GenericType<T> type, InputStream stream, Headers requestHeaders, Headers responseHeaders) {
		return read(type, stream, contentTypeCharset(responseHeaders));
	}

	@SuppressWarnings("unchecked")
	private T read(GenericType<T> type, InputStream in, Charset charset) {
		try (Reader r = new InputStreamReader(in, charset)) {
			return (T) jsonb.type(type.type()).fromJson(r);
		} catch (IOException e) {
			throw new JsonIoException("Failed to deserialize JSON to " + type, e);
		}
	}

	private Charset contentTypeCharset(Headers headers) {
		return headers.contentType()
				.flatMap(HttpMediaType::charset)
				.map(Charset::forName)
				.orElse(StandardCharsets.UTF_8);
	}
}
