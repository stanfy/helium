Adding a new code generator
===========================

Code generators are organized in separate modules each located at `{lang}/{target}`.
For example, a JSON scheme generator path is `json/json-scheme`.

In order to add a new code generator module, create a new directory and 
add `build.gradle` file with the following content
```groovy
apply plugin: 'groovy'
apply plugin: 'signing'
apply plugin: 'maven'
apply plugin: 'checkstyle'
apply plugin: 'codenarc'
apply from: file("$rootDir/gradle/common.gradle")

evaluationDependsOn(':codegen')
```

This code will declare a new Groovy project. It will also apply apply plugins require
for the module publication and static code analysis. Also it will ensure that you have
a compile time dependency on the main `helium` module that provides you required integration points.

You can use pure Java, just replace
`apply plugin: 'groovy'` with `apply plugin: 'java'` and remove the line with
`apply plugin: 'codenarc'`.

Also edit `settings.gradle` file in the root of this project and add your sub-project:
```groovy
include ':codegen:{your-lang}:{your-target}'
```

Now to add the first code, you need to create a class that implements `Handler` interface.

```java
public class MyLangCodeGenerator implements Handler {

  @Override
  public void handle(Project project) {
  }

}
```

The [Project](https://github.com/stanfy/helium/blob/master/helium/src/main/groovy/com/stanfy/helium/model/Project.java) class is a facade that allows you access the specification model created with
Helium DSL. You can list all services or declared types, service methods and do whatever you need wit them,

When your class implementation is ready and tested, you can consider adding it to 
[the command line tool](https://github.com/stanfy/helium/tree/master/command-line#adding-a-new-handler) 
and/or the gradle plugin. 
Remember to add a dependency on your module to the tool you integrate it with.
For example, in `build.gradle` of command-line:
```groovy
dependencies {
  // ...
  compile ':codegen:{your-lang}:{your-target}'
}
```
