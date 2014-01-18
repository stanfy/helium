#!/usr/bin/env groovy
println "Twitter Access"

def consumerKey = args[0]
def secret = args[1]

String.metaClass.encodeURL = { java.net.URLEncoder.encode(delegate, 'UTF-8') }

def auth = "${consumerKey.encodeURL()}:${secret.encodeURL()}".getBytes('UTF-8').encodeBase64().toString()

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6')
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


println "Sending token request..."
new HTTPBuilder('https://api.twitter.com').request(POST) {
  uri.path = '/oauth2/token'
  headers.'Authorization' = "Basic $auth"
  headers.'User-Agent' = "Twitter App Setup Script 1.0"
  send URLENC, ['grant_type': 'client_credentials']

  response.success = { resp, reader ->
    System.out << reader
    println ''
  }
  response.failure = { resp ->
    println "Got ${resp.statusLine.statusCode}: ${resp.statusLine.reasonPhrase}"
  }
}
