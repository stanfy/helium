Helium Command Line Tool
========================

Build jar with
```
./gradlew :command-line:noDepsJar
```

Get it at `command-line/build/libs/helium-cli.jar`.

Run
```bash
java -jar helium-cli.jar
```
to see usage instructions.

Download [link](TODO).

Adding a new handler
--------------------

In [Main.groovy](src/main/groovy/com/stanfy/helium/cli/Main.groovy) edit the `HANDLERS` map.

Example. Adding an "asm-x86-generator":
```groovy
private static final def HANDLERS = [
  // ...
  
  // handler name used in command line
  "asm-x86-generator" : [
      // description that will appear in usage description
      description: "Generate x86 assembler code for parsing messages",
      // how we can parametrize our generator
      properties: [
          "add-comments": "Whether to add comments to the generated code. Optional."
      ],
      // closure creating a handler instance
      factory: { def options, File output ->
        // resolve options
        boolean addComments = property(options, "add-comments") as boolean
        // create generator instance that implements Handler
        return new Asm86Generator(output, addComments)
      }
  ]
]
```

Now you can run your generator with 
```bash
java -jar helium-cli.jar --asm-x86-generator -Hadd-comments=true
```
