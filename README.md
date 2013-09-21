Helium
======

DSL for REST API description

Example
-------

```groovy
type "PersonProfile" message {
  id long required
  name string required
  dob timestamp optional
}

service {
  name 'Some web-service API'
  description 'bla bla bla'
  version 1

  location "http://api-example.com/api/${version}"

  get "/person/${id}" spec {
    name 'Get Person'
    description 'bla bla bla'
    parameters {
      full 'bool' required
      param(name : 'friends', type : 'bool', description : 'whether to include the friends list to the response')
    }
    response "PersonProfile"
  }

  post "/person/${id}" spec {
    name "Edit person profile"
    body "PersonProfile"
  }

}
```

