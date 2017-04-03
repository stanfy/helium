package com.stanfy.helium.model.tests;

import com.squareup.okhttp.Interceptor;

/** Interface marker for authentication parameters. */
public interface AuthParams {

  Interceptor httpInterceptor();

}
