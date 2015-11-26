package com.stanfy.helium.handler.codegen.java.retrofit

import com.stanfy.helium.internal.dsl.ProjectDsl
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
    project.type 'BList' sequence 'BMessage'
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

      get "/list" spec {
        response 'BList'
      }

      get '/array/parameters' spec {
        name 'Check array parameters'
        parameters {
          state 'int32' sequence
        }
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

  def "should emit entity imports"() {
    when:
    options.entitiesPackage = "another"
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains("import another.AMessage;")
    text.contains("import another.BMessage;")
    text.contains("import retrofit.client.Response;")
  }

  def "should not emit same package entity imports"() {
    when:
    options.packageName = "my.pckg"
    options.entitiesPackage = "my.pckg"
    gen.handle(project)
    def text = new File("$output/my/pckg/A.java").text

    then:
    !text.contains(".AMessage;")
    !text.contains(".BMessage;")
    !text.contains("include BMessage;")
    !text.contains("include AMessage;")
    text.contains("import retrofit.client.Response;")
  }

  def "should not emit entity imports if package is not specified"() {
    when:
    options.packageName = "my.pckg"
    gen.handle(project)
    def text = new File("$output/my/pckg/A.java").text

    then:
    !text.contains(".AMessage;")
    !text.contains(".BMessage;")
    !text.contains("import AMessage;")
    !text.contains("import BMessage;")
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

  def "should not import form body wrappers"() {
    given: "post with form"

    project.type 'string'
    project.service {
      name "TheService"

      post "/users" spec {
        name "register_user"
        parameters {
          department 'string'
        }
        body form {
          firstname 'string'
          lastname 'string'
        }
      }
    }

    when: "dsl is parsed"
    gen.handle project
    def text = new File("$output/test/api/TheService.java").text

    then: "result interface should not contain invalid imports"
    !text.contains("import post_users_body_POST;")
  }

  def "should write generic data body"() {
    given:
    project.service {
      name "DataService"
      post "/data" spec {
        body data()
      }
    }

    when:
    gen.handle project
    def text = new File("$output/test/api/DataService.java").text

    then:
    text.contains "import retrofit.mime.TypedOutput;"
    text.contains "@POST(\"/data\")"
    text.contains "Response postData(@Body TypedOutput body);"
  }

  def "should write multipart map body"() {
    given:
    project.service {
      name "MultipartService"
      post "/multipart" spec {
        name 'upload'
        body multipart()
      }
    }

    when:
    gen.handle project
    def text = new File("$output/test/api/MultipartService.java").text

    then:
    text.contains "@Multipart"
    text.contains "@POST(\"/multipart\")"
    text.contains "Response upload(@PartMap Map<String, Object> parts)"
  }

  def "should write multipart body with fields"() {
    given:
    project.type 'string'

    project.service {
      name "MultipartService"
      post "/multipart" spec {
        name 'upload'
        body multipart {
          name 'string'
          file1 file()
          data1 data()
        }
      }
    }

    when:
    gen.handle project
    def text = new File("$output/test/api/MultipartService.java").text

    then:
    text.contains "@Multipart"
    text.contains "@POST(\"/multipart\")"
    text.contains "Response upload"
    text.contains "@Part(\"name\") String name"
    text.contains "@Part(\"file1\") TypedFile file1"
    text.contains "@Part(\"data1\") TypedOutput data1"
  }

  def "should respect rxObservables option"() {
    setup:
    project = new ProjectDsl()

    project.type 'int32'
    project.type 'string'
    project.type 'SomeResponse' message {}
    project.type 'TheRequest' message {}

    project.service {
      name 'ReactiveService'
      location 'https://github.com/ReactiveX/RxJava'

      post "/add" spec {
        response 'SomeResponse'
        body 'TheRequest'
      }

      get "/list" spec {
        parameters {
          count 'int32'
          name 'string'
        }
        response 'SomeResponse'
      }

    }
    options.customPrimitivesMapping = [
      'string' : 'java.lang.String'
    ]


    when:
    options.useRxObservables = true
    and:
    gen.handle project
    def text = new File("$output/test/api/ReactiveService.java").text

    then:
    text.contains "rx.Observable"
    text.contains "Observable<SomeResponse> postAdd(@Body TheRequest body)"
    text.contains "Observable<SomeResponse> getList(@Query(\"count\") int count, @Query(\"name\") String name)"

  }

  def "should respect sequences in request parameters"() {
    when:
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains 'Response checkArrayParameters(@Query("state") List<Integer> state)'
  }

  def "should respect sequences in request parameters (arrays)"() {
    when:
    options.useArraysForSequences()
    gen.handle(project)
    def text = new File("$output/test/api/A.java").text

    then:
    text.contains 'Response checkArrayParameters(@Query("state") int[] state)'
  }

}
