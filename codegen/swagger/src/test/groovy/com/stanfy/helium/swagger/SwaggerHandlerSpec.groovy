package com.stanfy.helium.swagger

import com.stanfy.helium.Helium
import com.stanfy.helium.model.Project
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import spock.lang.Ignore
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
          }
        }
        .getProject()
  }

  private File uberSpec() {
    return new File(dir, "Uber_API.json")
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

  @Ignore("will be implemented soon")
  def "filling definitions"() {
    given:
    handler.handle(project)
    def data = specData(uberSpec())

    expect:
    data.definitions?.size() > 1
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
    data.paths.'/products'.get.description?.contains 'The Products endpoint'
    data.paths.'/products'.get.parameters?.size() > 0
    data.paths.'/products'.get.parameters[0].name == 'latitude'
    data.paths.'/products'.get.parameters[0].in == 'query'
    data.paths.'/products'.get.parameters[0].description == 'Latitude component of location.'
    data.paths.'/products'.get.parameters[0].required
    data.paths.'/products'.get.parameters[0].type == 'number'
    data.paths.'/products'.get.parameters[0].format == 'double'
    data.paths.'/products'.get.parameters[1].name == 'longitude'
  }

  void cleanup() {
    FileUtils.deleteDirectory(dir)
  }

}