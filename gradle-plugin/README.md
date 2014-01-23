Helium Gradle Plugin
====================


Add Helium gradle plugin dependency to your build script.
```groovy
buildscript {
 repositories {
   mavenCentral()
 }
 dependencies {
   classpath 'com.stanfy.helium:gradle-plugin:0.3.+'
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

Now you can run generated tests with
```
gradle runApiTests
```

Optionally specify whether to ignore test failures.
```groovy
helium {
 specification file('my.api')
 ignoreFailures true
}
```

Add Java source generation:
```groovy
helium {
 specification file('my.api')
 sourceGen {
   entities {
     output file("$buildDir/source/rest-api")
     options {
       packageName = "com.example.data"
       prettifyNames = true
     }
   }
   constants {
     output file("$buildDir/source/constants")
     options {
       packageName = "com.example"
     }
   }
 }
}
```

Currently you may generate `entities` and `constants`. You must specify package name for generated source code in
`options`.

The plugin will add a separate task for each generation declaration like `generateEntitiesComExampleData`.
You also may access these added tasks with specified Java package names:
```
def genEntitiesTask = helium.sourceGen.entities['com.example.data']
println genEntitiesTask.output // prints generates sources location
```
