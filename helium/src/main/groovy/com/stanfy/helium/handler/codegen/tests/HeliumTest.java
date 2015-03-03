package com.stanfy.helium.handler.codegen.tests;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Helium test tools.
 */
public final class HeliumTest {

  private static final Logger LOG = LoggerFactory.getLogger(HeliumTest.class);

  private HeliumTest() {
    throw new UnsupportedOperationException("no instances");
  }

  /**
   * Provides HTTP client used in tests.
   * The client has interceptors that store request/response body in memory allowing to read them multiple times.
   */
  public static OkHttpClient httpClient() {
    // TODO: Use one client.
    OkHttpClient client = new OkHttpClient();

    // Add logging and memory-backed request/response body.
    client.interceptors().add(new Interceptor() {
      @Override
      public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        LOG.info("---> HTTP {} {}", request.method(), request.urlString());
        logHeaders(request.headers());
        if (request.body() != null) {
          Buffer bodyBuffer = new Buffer();
          request.body().writeTo(bodyBuffer);

          final ByteString body = bodyBuffer.readByteString();
          final MediaType contentType = request.body().contentType();

          logBytes(body, contentType);
          LOG.info("---> END HTTP ({} body)", body.size());

          // Substitute request instance.
          request = request.newBuilder()
              .method(request.method(), new RequestBody() {
                @Override
                public long contentLength() throws IOException {
                  return body.size();
                }

                @Override
                public MediaType contentType() {
                  return contentType;
                }

                @Override
                public void writeTo(final BufferedSink sink) throws IOException {
                  sink.write(body);
                  sink.close();
                }
              })
              .build();
        }

        long start = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long time = System.currentTimeMillis() - start;

        LOG.info("<--- HTTP {} {} ({}ms)", request.urlString(), response.code(), time);
        logHeaders(response.headers());
        BufferedSource source = response.body().source();
        try {
          final ByteString body = source.readByteString();
          final MediaType contentType = response.body().contentType();

          logBytes(body, contentType);
          LOG.info("<--- END HTTP ({} body)", body.size());

          return response.newBuilder()
              .body(new ResponseBody() {
                @Override
                public MediaType contentType() {
                  return contentType;
                }

                @Override
                public long contentLength() {
                  return body.size();
                }

                @Override
                public BufferedSource source() {
                  Buffer buffer = new Buffer();
                  buffer.write(body);
                  return buffer;
                }
              })
              .build();
        } finally {
          source.close();
        }
      }
    });
    return client;
  }

  private static void logHeaders(final Headers headers) {
    int count = headers.size();
    if (count == 0) {
      return;
    }
    for (int i = 0; i < count; i++) {
      LOG.info("{}: {}", headers.name(i), headers.value(i));
    }
  }

  private static void logBytes(final ByteString body, final MediaType contentType) {
    Charset charset = contentType != null ? contentType.charset() : null;
    if (charset == null || charset.equals(Charset.forName("UTF-8"))) {
      LOG.info(body.utf8());
    } else {
      LOG.info(new String(body.toByteArray(), charset));
    }
  }

  /** Helium tests runner. */
  public static class Runner extends BlockJUnit4ClassRunner {

    public Runner(final Class<?> klass) throws InitializationError {
      super(klass);
    }
  }

}
