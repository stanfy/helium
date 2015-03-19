package com.stanfy.helium.handler.codegen.java.retrofit

import com.stanfy.helium.dsl.ProjectDsl
import spock.lang.Specification
/**
 * Spec for RetrofitInterfaceGenerator.
 */
class RetrofitInterfaceGeneratorSpec extends Specification {

  ProjectDsl project

  RetrofitInterfaceGenerator gen
  RetrofitGeneratorOptions options
  File output

  def setup() {
    output = File.createTempDir()
    options = RetrofitGeneratorOptions.defaultOptions("test.api")
    gen = new RetrofitInterfaceGenerator(output, options)

    project = new ProjectDsl()
    project.type 'int32' spec { }
    project.type 'AMessage' message { }
    project.type 'BMessage' message { }
    project.service {
      name "A"
      location "http://www.stanfy.com"

      get "/get/void" spec { }
      post "/post/complex/@id" spec {
        name "Post something complex"
        parameters {
          a 'int32'
        }
        body 'AMessage'
        response 'BMessage'
      }

      delete "/example" spec {
        name "Delete stuff"
      }

      get "/headers" spec {
        httpHeaders 'H1', 'H2', header('HC1', 'cvalue1'), header('HC2', 'cvalue2')
        response 'BMessage'
      }

    }
    project.service {
      name "B"
    }
  }

  def "should generate interfaces"() {
    when:
    gen.handle(project)

    then:
    new File("$output/test/api/A.java").exists()
    new File("$output/test/api/B.java").exists()
  }

  def "should write default location"() {
    when:
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains("String DEFAULT_URL = \"http://www.stanfy.com\"")
  }

  def "should emit imports"() {
    when:
    options.entitiesPackage = "another"
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains("import another.AMessage;")
    text.contains("import another.BMessage;")
    text.contains("import retrofit.client.Response;")
  }

  def "should write methods"() {
    when:
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains('@GET("/get/void")\n')
    text.contains('Response getGetVoid();\n')
  }

  def "should write different parameters"() {
    when:
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains('@POST("/post/complex/{id}")\n')
    text.contains(
        'BMessage postSomethingComplex(@Path("id") String id, @Query("a") int a, @Body AMessage body);\n'
    )
  }

  def "can use method names"() {
    when:
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains('@DELETE("/example")\n')
    text.contains('Response deleteStuff()')
  }

  def "maps headers"() {
    when:
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains('@Headers({')
    text.contains('"HC1: cvalue1",')
    text.contains('"HC2: cvalue2"')
    text.contains('BMessage getHeaders(@Header("H1") String headerH1, @Header("H2") String headerH2)')
  }

  def "good message for missing service name"() {
    when:
    ProjectDsl p = new ProjectDsl()
    p.service { }
    gen.handle(p)

    then:
    def e = thrown(IllegalStateException)
    e.message.contains "service name"
  }

  def "should write form body types"() {
    given:
    project.type "FormMessage" message {
      count 'int32'
    }
    project.service {
      name "FormService"
      post "/form" spec {
        body form("FormMessage")
      }
    }

    when:
    gen.handle(project)
    def text = new File("$output/test/api/FormService.java").text

    then:
    text == """
package test.api;

import retrofit.client.Response;
import retrofit.http.*;

public interface FormService {

  @POST("/form")
  @FormUrlEncoded
  Response postForm(@Field("count") int count);

}""".trim() + '\n'
  }

}
