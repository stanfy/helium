package com.stanfy.helium.dsl

/**
 * Example of Helium specification.
 */
final class SpecExample {

  static def example = {

    note "Twitter REST API example"

    type "UserProfile" message {
      id long required
      screen_name 'string' required
      profile_image_url_https 'string' optional
    }

    service {
      name 'Twitter API'
      description 'Piece of Twitter API'
      version 1.1

      location "https://api.twitter.com/${version}"

      get "/users/show.json" spec {
        name 'Get user profile'
        description '''
          Returns a variety of information about the user specified by the required user_id
          or screen_name parameter. The author's most recent Tweet will be returned inline when possible.
        '''
        parameters {
          user_id long optional
          screen_name 'string' required
          include_entities 'bool' optional
        }
        response "UserProfile"
      }

      get "/test/@param.json" spec {
        name "Test method"
        response "UserProfile"
        tests {
          pathExample param: 'value'
        }
      }

      get "/simple/request" spec {
        name "Simple request without required parameters"
        parameters {
          a 'string' optional
        }
        response "UserProfile"
      }

      get "/required/@example" spec {
        name "Example with required parameters and parametrized path"
        parameters {
          param1(type: 'int32', required: true, examples: ['2'])
        }
        response "UserProfile"
        tests {
          pathExample {
            'example' 'HOP'
          }
          httpHeaders {
            'Super-Header' 'A'
          }
        }
      }

      get "product/get" spec {
        name 'Get test product'
        parameters {
          id(type: 'int64', required: true, examples: ['23288'])
        }
        response "int32"
      }

      post "post/@example" spec {
        name "Post example"
        tests {
          pathExample {
            'example' '123'
          }
        }
        parameters {
          full(type: 'bool', required: false, examples: ['false'])
        }
        body 'UserProfile'
        response 'int64'
      }

      tests {
        useExamples true
        httpHeaders {
          'User-Agent' 'Mozilla'
        }
      }

    }

  }

}
