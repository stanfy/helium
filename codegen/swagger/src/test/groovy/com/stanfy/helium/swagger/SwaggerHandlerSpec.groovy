package com.stanfy.helium.swagger

import com.stanfy.helium.Helium
import com.stanfy.helium.model.Project
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import spock.lang.Specification

class SwaggerHandlerSpec extends Specification {

  private SwaggerHandler handler
  private File dir
  private Project project

  void setup() {
    this.dir = File.createTempDir('helium', 'swagger')
    this.handler = new SwaggerHandler(dir)
    this.project = new Helium()
        .defaultTypes()
        .from {
          note 'Swagger Example'

          type 'Product' message {
            product_id(type: 'string')  {
              description '''
                Unique identifier representing a specific product for a given latitude & longitude.
                For example, uberX in San Francisco will have a different product_id than uberX in Los Angeles.
              '''
            }
            description(type: 'string', description: 'Description of product.')
            display_name(type: 'string', description: 'Display name of product.')
            capacity(type: 'int32', description: 'Capacity of product. For example, 4 people.')
            image(type: 'string', description: 'Image URL representing the product.')
          }

          type 'ProductList' sequence 'Product'

          type 'User' message {
            last_name 'string'
            first_name 'string'
          }

          type 'SomeData' message {
            field 'string'
            user 'User'
          }

          service {
            name 'Uber API'
            description 'Move your app forward with the Uber API'
            version '1.0.0'

            location "https://api.uber.com/v1"

            get '/products' spec {
              name 'Product Types'
              description '''
                The Products endpoint returns information about the Uber products offered at a given location.
                The response includes the display name and other details about each product,
                and lists the products in the proper display order.
              '''
              parameters {
                latitude(type: 'double', description: 'Latitude component of location.')
                longitude(type: 'double', description: 'Longitude component of location.')
                server_token(type: 'string', description: 'API key.')
              }
              response 'ProductList'
            }

            post '/body-test' spec {
              body 'SomeData'
              response 'ProductList'
            }

            get '/products/@id' spec {
              name 'Product details'
              response {
                hash 'string'
                product 'Product'
              }
            }
          }

          service {
            name "Test API 1"
            location "http://localhost"

            delete '/test/resource' spec {
              description 'Delete something'
            }

            post '/multipart' spec {
              name 'Multipart test'
              body multipart('form-data') {
                publickey file()
              }
            }
          }
        }
        .getProject()
  }

  private File uberSpec() {
    return new File(dir, "Uber_API.json")
  }

  private File testSpec() {
    return new File(dir, "Test_API_1.json")
  }

  private static def specData(File file) {
    return new JsonSlurper().parse(file)
  }

  def "produce swagger spec"() {
    given:
    handler.handle(project)

    expect:
    uberSpec().exists()
  }

  def "filling info"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.swagger == '2.0'
    data.info?.title == 'Uber API'
    data.info?.description?.contains 'forward'
    data.info?.version == '1.0.0'
  }

  def "filling base path and schemes"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.host == 'api.uber.com'
    data.schemes == ['https']
    data.basePath == '/v1'
  }

  def "filling definitions"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.definitions?.size() > 0
    data.definitions.Product != null
    data.definitions.Product.properties.display_name != null
    data.definitions.Product.properties.capacity != null
    data.definitions.Product.properties.capacity.type == 'integer'
    data.definitions.Product.properties.image != null
    data.definitions.Product.properties.product_id.type == 'string'
    data.definitions.Product.properties.description.description == 'Description of product.'
    data.definitions.ProductList != null
  }

  def "filling paths"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.paths?.size() > 0
    data.paths.'/products' != null
    data.paths.'/products'.get != null
    data.paths.'/products'.get.summary == 'Product Types'
    data.paths.'/products'.get.operationId == 'productTypes'
    data.paths.'/products'.get.description?.contains 'The Products endpoint'
    data.paths.'/products'.get.parameters?.size() > 0
    data.paths.'/products'.get.parameters[0].name == 'latitude'
    data.paths.'/products'.get.parameters[0].in == 'query'
    data.paths.'/products'.get.parameters[0].description == 'Latitude component of location.'
    data.paths.'/products'.get.parameters[0].required
    data.paths.'/products'.get.parameters[0].type == 'number'
    data.paths.'/products'.get.parameters[0].format == 'double'
    data.paths.'/products'.get.parameters[1].name == 'longitude'
    data.paths.'/products'.get.responses != null
    data.paths.'/products'.get.responses.'200' != null
    data.paths.'/products'.get.responses.'200'.description != null
    data.paths.'/products'.get.responses.'200'.schema?.$ref == '#/definitions/ProductList'
  }

  def "define body"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.paths?.size() > 0
    data.paths.'/body-test'?.post != null
    data.paths.'/body-test'.post.operationId == 'postBodytest'
    data.paths.'/body-test'.post.parameters?.size() > 0
    data.paths.'/body-test'.post.parameters[0].name == 'body'
    data.paths.'/body-test'.post.parameters[0].in == 'body'
    data.paths.'/body-test'.post.parameters[0].required
    data.paths.'/body-test'.post.parameters[0].schema?.$ref == '#/definitions/SomeData'
    data.definitions?.SomeData != null
  }

  def "path templating"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.paths?.size() > 0
    data.paths.'/products/{id}'?.get != null
    data.paths.'/products/{id}'.get.operationId == 'productDetails'
    data.paths.'/products/{id}'.get.parameters?.size() == 1
    data.paths.'/products/{id}'.get.parameters[0].in == 'path'
    data.paths.'/products/{id}'.get.parameters[0].name == 'id'
    data.paths.'/products/{id}'.get.parameters[0].type == 'string'
    data.paths.'/products/{id}'.get.parameters[0].required
  }

  def "root base path"() {
    given:
    handler.handle(project)
    def data =specData(testSpec())

    expect:
    data.basePath == '/'
  }

  def "nested definitions"() {
    given:
    handler.handle(project)
    def data =specData(uberSpec())

    expect:
    data.definitions?.'SomeData'?.properties?.user?.$ref == '#/definitions/User'
    data.definitions.'User' != null
  }

  def "anonymous definitions"() {
    given:
    handler.handle(project)
    def data =specData(uberSpec())

    expect:
    data.definitions?.'Product' != null
    data.paths?.'/products/{id}'?.get?.responses?.'200'?.schema?.type == 'object'
    data.paths.'/products/{id}'.get.responses.'200'.schema.required == ['hash', 'product']
  }

  def "undefined responses"() {
    given:
    handler.handle(project)
    def data =specData(testSpec())

    expect:
    data.paths?.'/test/resource'?.delete?.responses?.'200'?.description != null
  }

  def "multipart body"() {
    given:
    handler.handle(project)
    def data = specData(testSpec())

    expect:
    data.paths?.'/multipart'?.post != null
    data.paths.'/multipart'.post.operationId == 'multipartTest'
    data.paths.'/multipart'.post.parameters?.size() == 1
    data.paths.'/multipart'.post.parameters[0].name == 'publickey'
    data.paths.'/multipart'.post.parameters[0].in == 'formData'
    data.paths.'/multipart'.post.parameters[0].required
    data.paths.'/multipart'.post.parameters[0].type == 'file'
    data.paths.'/multipart'.post.consumes == ['multipart/form-data']
  }

  def "respect includes in options"() {
    given:
    def options = new SwaggerOptions()
    options.includes('GET /products')
    handler = new SwaggerHandler(dir, options)
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.paths?.keySet() == ['/products'] as Set
    data.paths.'/products'.keySet() == ['get'] as Set
  }

  void cleanup() {
    FileUtils.deleteDirectory(dir)
  }

}
