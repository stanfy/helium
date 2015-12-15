package com.stanfy.helium.sample.retrofit;

import com.stanfy.helium.sample.retrofit.api.MyBackend;

import retrofit.RestAdapter;

import java.util.Arrays;

/**
 * Creates a {@link retrofit.RestAdapter} and API implementation.
 */
public class Sample {

  public static MyBackend setup() {
    return new RestAdapter.Builder()
        .setEndpoint(MyBackend.DEFAULT_URL)
        .setLogLevel(RestAdapter.LogLevel.HEADERS)
        .build()
        .create(MyBackend.class);
  }

}
