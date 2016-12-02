package com.stanfy.helium.format.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.MediaType;
import com.stanfy.helium.DefaultType;
import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.FormatWriter;
import okio.Okio;
import okio.Sink;
import okio.Source;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Provides a JSON format.
 */
public final class JsonFormatProvider {

  private JsonFormatProvider() { }

  static boolean supportsMediaType(MediaType type) {
    return type.subtype().endsWith("json");
  }

  public static class Reader implements FormatReader.Factory {

    @Override
    public boolean supportsMediaType(MediaType type) {
      return JsonFormatProvider.supportsMediaType(type);
    }

    @Override
    public JsonReadFormat create(Source source, Charset charset) {
      InputStreamReader in = new InputStreamReader(Okio.buffer(source).inputStream(), charset);
      JsonReader reader = new JsonReader(in);
      reader.setLenient(true);
      JsonReadFormat format = new JsonReadFormat(reader);
      for (DefaultType t : DefaultType.values()) {
        format.registerPrimitiveAdapter(t.getType(), Primitives.converterFor(t.getType()));
      }
      return format;
    }

  }

  public static class Writer implements FormatWriter.Factory {

    @Override
    public boolean supportsMediaType(MediaType type) {
      return JsonFormatProvider.supportsMediaType(type);
    }

    @Override
    public FormatWriter create(Sink sink, Charset charset) {
      OutputStreamWriter out = new OutputStreamWriter(Okio.buffer(sink).outputStream(), charset);
      JsonWriter writer = new JsonWriter(out);
      writer.setLenient(true);
      JsonWriteFormat format = new JsonWriteFormat(writer);
      for (DefaultType t : DefaultType.values()) {
        format.registerPrimitiveAdapter(t.getType(), Primitives.converterFor(t.getType()));
      }
      return format;
    }
  }

}
