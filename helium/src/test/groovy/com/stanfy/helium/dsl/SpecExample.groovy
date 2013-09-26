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

    }

  }

}
