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

// read from file and generate POJOs
new Helium().from(new File("twitter.spec"))
  .processBy(new PojoGenerator(
      new File("/build/gen"),
      PojoGeneratorOptions.defaultOptions("com.example.twitter")
  ));
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


Gradle plugin usage
-------------------
Add Helium gradle plugin dependency to your build script.
```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.stanfy.helium:gradle-plugin:0.1'
  }
}
```

Apply Helium plugin.
```groovy
apply plugin:'helium'
```

Point out where your specification is located.
```groovy
helium {
  specification file('my.api')
}
```

Optionally specify whether to ignore test failures.
```groovy
helium {
  specification file('my.api')
  ignoreFailures true
}
```

Add POJO generation task:
```groovy
helium {
  specification file('my.api')
  pojo {
    output file("$buildDir/source/rest-api")
    options {
      prettifyNames = true
    }
  }
}
```


License
-------

     Copyright 2013 Stanfy Corp.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
