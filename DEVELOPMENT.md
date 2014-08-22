Helium Development
==================

Project structure
-----------------

There are several modules.
- `helium` - core Java/Groovy library for parsing/processing Helium DSL
- `gradle-plugin` - Gradle plugin that allows to add Helium spec files to your Gradle projects, 
  configure sources generation
- `command-line` - command line tool for processing Helium specs

Please see README files in modules to get more details on every subproject.

Building
--------

Helium can be built with Gradle. You do not need to install Gradle manually. Just use Gradle wrapper script
(`gradlew` or `gradlew.bat` depending on your platform).

**Running unit tests:**
```bash
./gradlew test
```

**Running integration tests:**
```bash
./gradlew install intTest
```

Contributing
------------
Just make a pull request on Github. Before submitting a PR, please run
```bash
./gradlew clean check install intTest
```
to verify your changes.

Note that your classes and methods must be covered with unit tests.

Main classes
------------
- Package `com.stanfy.helium.model` - classes that represent Helium specification model.
- Interface `com.stanfy.helium.handler.Handler` - something that can process a specification.
  All source code and test generators implement this interface.
- Package `com.stanfy.helium.handler.codegen.java` - Java source code generation handlers. 
- Package `com.stanfy.helium.handler.codegen.tests` - poke and scenario test handlers. 
