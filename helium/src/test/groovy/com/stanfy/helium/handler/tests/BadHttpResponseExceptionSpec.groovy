package com.stanfy.helium.handler.tests

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import spock.lang.Specification

class BadHttpResponseExceptionSpec extends Specification {

  def "curl string"() {
    given:
    def request = new Request.Builder()
        .method("PUT", RequestBody.create(MediaType.parse("application/json"), "{\"name\": 123}"))
        .addHeader("h1", "v1'escaped")
        .addHeader("h2", "v2")
        .url("http://example.com?a=b&c=d")
        .build()
    def error = new BadHttpResponseException(request, null)

    expect:
    error.curl() == "curl -X PUT -H 'h1: v1\\'escaped' -H 'h2: v2' -d '{\"name\": 123}' 'http://example.com?a=b&c=d'"
  }

}
