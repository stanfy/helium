package com.stanfy.helium.model.tests

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.Interceptor.Chain
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody

/** OAuth2 authentication parameters. */
class Oauth2AuthenticationParams implements AuthParams {

  private static final Gson OAUTH_GSON = new Gson()

  AuthType type

  String tokenUrl

  String clientId

  String clientSecret

  private Interceptor interceptor
  private String accessToken

  public void setType(String value) {
    try {
      this.type = AuthType.valueOf(value.toUpperCase(Locale.US))
    } catch (IllegalArgumentException e) {
      def supported = AuthType.values().collect { it.name().toLowerCase(Locale.US) }
      throw new IllegalArgumentException("Unknown oauth2 type $value. Supported types: $supported.")
    }
  }

  @Override
  Interceptor httpInterceptor() {
    if (!interceptor) {
      interceptor = { Chain chain ->
        def request = chain.request()
        if (accessToken) {
          request = request.newBuilder().header('Authorization', "Bearer $accessToken").build()
        }

        // We support client credentials only.
        def resp = chain.proceed(request)
        if (resp.code() == 401) {
          def url = "$tokenUrl?grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret"
          def tokenRequest = new Request.Builder()
              .url(url)
              .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
              .build()
          def tokenResp = chain.proceed(tokenRequest)
          if (tokenResp.code() == 200) {
            def reader = tokenResp.body().charStream()
            try {
              def data = OAUTH_GSON.fromJson(reader, AuthTokenResponse)
              accessToken = data.accessToken
              def newRequest = request.newBuilder().header("Authorization", "Bearer $accessToken").build()
              resp = chain.proceed(newRequest)
            } finally {
              reader.close()
            }
          }
        }
        return resp
      } as Interceptor
    }
    return interceptor
  }

  enum AuthType {
    CLIENT_CREDENTIALS
  }

  private static class AuthTokenResponse {
    @SerializedName("access_token")
    String accessToken
  }

}
