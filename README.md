Helium
======

Helium is a DSL for REST API specifications and also a Java API for processing descriptions written in this language.

Documentation
-------------
Project is in ongoing development. JSON is currently the only supported format.
Read out [wiki](https://github.com/stanfy/helium/wiki/Helium).

Specification Example
---------------------

```groovy
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
      screen_name 'string' optional
      include_entities boolean optional
    }
    response "UserProfile"
  }
}

```

Java API Examples
-----------------

```java
// read from string
new Helium().from("service {name 'Example Service'}").processBy(new Handler() {
  public void process(Project p) {
    System.out.println(p.getServices());
  }
});

// read from file
new Helium().from(new File("twitter.spec")).processBy(new Handler() {
  public void process(Project p) {
    System.out.println(p.getServices());
  }
});
```

*With Groovy :)*

You can write the specification code directly in your client:
```groovy
new Helium().from {
  service {
    name 'Example service'
  }
}.processBy({ Project p ->
  println p.services
} as Handler)
```
