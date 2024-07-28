package com.github.russp.jtorrt.support.http.media;

import io.avaje.jsonb.Jsonb;
import io.helidon.common.GenericType;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderValues;
import io.helidon.http.Headers;
import io.helidon.http.HttpMediaType;
import io.helidon.http.WritableHeaders;
import io.helidon.http.media.EntityWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Optional;

public class AvajeJsonbWriter<T> implements EntityWriter<T> {

	private final Jsonb jsonb;

	public AvajeJsonbWriter(Jsonb jsonb) {
		this.jsonb = jsonb;
	}

	@Override
	public void write(GenericType<T> type, T object, OutputStream outputStream, Headers requestHeaders, WritableHeaders<?> responseHeaders) {
		responseHeaders.setIfAbsent(HeaderValues.CONTENT_TYPE_JSON);

		for (HttpMediaType acceptedType : requestHeaders.acceptedTypes()) {
			if (acceptedType.test(MediaTypes.APPLICATION_JSON)) {
				Optional<String> charset = acceptedType.charset();
				if (charset.isPresent()) {
					Charset characterSet = Charset.forName(charset.get());
					write(type, object, new OutputStreamWriter(outputStream, characterSet));
				} else {
					write(type, object, outputStream);
				}
				return;
			}
		}

		write(type, object, outputStream);
	}

	@Override
	public void write(GenericType<T> type, T object, OutputStream outputStream, WritableHeaders<?> headers) {
		headers.setIfAbsent(HeaderValues.CONTENT_TYPE_JSON);

		write(type, object, outputStream);
	}

	private void write(GenericType<T> type, T object, Writer writer) {
		jsonb.type(type.type()).toJson(object, writer);
	}

	private void write(GenericType<T> type, T object, OutputStream outputStream) {
		jsonb.type(type.type()).toJson(object, outputStream);
	}
}
