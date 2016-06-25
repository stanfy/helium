package com.stanfy.helium.cli

import com.stanfy.helium.Helium
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGenerator
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCMappingOption
import com.stanfy.helium.handler.codegen.swift.entity.*

/**
 * Main entry point.
 */
class Main {

  /**
   * This is a map of command line options to Helium spec handlers.
   * If you want to add  new handler invoked by a user with
   * <pre>
   *   java helium-cli.jar -my-handler -Hfoo=value
   * </pre>
   * add the following entry to this map:
   * <pre>
   *   'my-handler': [
   *     description: 'These notes will be displayed in command line help',
   *     properties: [
   *       'foo': 'Document your parameter'
   *     ],
   *     // Output is defined as -o <dir> in the command line.
   *     factory: { def options, File output ->
   *       return new MyHandler(options.requiredProperty('foo'), output)
   *     }
   *   ]
   * </pre>
   */
  private static final def HANDLERS = [
      "java-entities" : [
          description: "Generate Java entity classes",
          properties: [
              "package": "Package name for generated classes. Required."
          ],
          factory: { def options, File output ->
            EntitiesGeneratorOptions genOptions = EntitiesGeneratorOptions.defaultOptions(
                requiredProperty(options, "package")
            )
            return new EntitiesGenerator(output, genOptions)
          }
      ],
      "objective-c-entities" : [
              description: "Generate Objective-C entity classes",
              properties: [
                      "prefix": "Prefix for generated classes. Required.",
                      "customMapping" : "Type mappings. Can be specified multiple times. Optional. usage: -HcustomMapping=HELIUM_TYPE:OBJC_TYPE(:IS_REFERENCE)",
                      "customValueTransformer" : "Mantle Custom value transformers for specific messages. Optional. usage: -customValueTransformer=HELIUM_TYPE:OBJC_TRANSFORMER_TYPE",
                      "mappingType" : "Mapping type. Optional. Possible values : mantle, sfmapping usage: -mappingType=mantle|sfmapping"
              ],

              factory: { def options, File output ->
                  ObjCEntitiesOptions genOptions = new ObjCEntitiesOptions()
                  genOptions.prefix = requiredProperty(options, "prefix");
                  genOptions.customTypesMappings = mapProperty(options, "customMapping")
                  genOptions.mantleCustomValueTransformers = mapProperty(options, "customValueTransformer")
                  def mappingType
                  switch (property(options, "mappingType")) {
                  case "mantle":
                    mappingType = ObjCMappingOption.MANTLE
                    break
                  case "sfmapping":
                    mappingType = ObjCMappingOption.SFMAPPING
                    break
                  default:
                    mappingType = ObjCMappingOption.NONE
                    break
                }
                genOptions.mappingsType = mappingType
                return new ObjCEntitiesGenerator(output, genOptions)
              }
      ],
      "swift-entities": [
          description: "Generate Swift entity classes",
          properties:  [
              "prop" : "value"
          ],
          factory: { def options, File output ->
            SwiftGenerationOptions generationOptions  =  new SwiftGenerationOptions()
            return new SwiftEntitiesGenerator(output, generationOptions)
          }
      ]

  ]

  private static final def CLI = new CliBuilder(usage: "java -jar helium-cli.jar [options] <spec>", header: "Options:")
  static {
    CLI.x("Do not include default types")
    CLI.H(args: 2, valueSeparator: '=', argName: 'property=value', "Set value of a property\n")
    CLI.o(longOpt: "output", args: 1, argName: 'dir', "Output directory\n")

    CLI.V(args: 2, valueSeparator: '=', argName: 'name=value', "Set variable accessible in specs\n")

    HANDLERS.each { name, definition ->
      String propsDescr = definition.properties.keySet().collect {
        "-H${it}=<value>:\n${definition.properties[it]}\n"
      }.inject("", {x, y -> x + y})
      CLI._(longOpt: name, "$definition.description\nUsed properties:\n$propsDescr\n")
    }

    CLI.width = 120
  }

  private static String requiredProperty(def options, String name) {
    String res = property(options, name)
    if (!res) {
      println "Property -H$name=<value> is required"
      System.exit(1)
    }
    return res
  }

  private static String property(def options, String name) {
    if (!options.Hs) {
      return null
    }
    def props = options.Hs as List
    for (int i = 0; i < props.size() / 2; i++) {
      if (name == props[i * 2]) {
        return props[i * 2 + 1]
      }
    }
    return null
  }

  /** Property, that can contain multiple values */
  private static Map<String, String> mapProperty(def options, String name) {
    if (!options.Hs) {
      return null
    }
    def res = [:]
    def props = options.Hs as List
    for (int i = 0; i < props.size() / 2; i++) {
      if (name == props[i * 2]) {
        def object = props[i * 2 + 1]
        def kv = (object as String).split(":")
        if (kv.length != 2) {
          println "Property -H$name=<key>:<value> is required"
          System.exit(1)
        }
        res[kv[0]] = kv[1];
      }
    }
    return res as Map
  }

  static void main(final String[] args) {
    def options = CLI.parse(args)
    def specs = options.arguments()
    if (!options || !specs) {
      println CLI.usage()
      System.exit(1);
      return;
    }

    File output = options.o ? new File(options.o as String) : new File(".")

    specs.each { String fileName ->
      def file = new File(fileName)
      if (!file.exists()) {
        println "File $file does not exist"
        System.exit(1)
      }

      def h = new Helium()
      if (!options.x) {
        h.defaultTypes()
      }

      setVariables(h, options)
      h.from(file)

      HANDLERS.each { name, definition ->
        if (options.hasOption(name)) {
          h.processBy(definition.factory(options, output) as Handler)
        }
      }
    }
  }

  private static void setVariables(final Helium h, final def options) {
    if (!options.Vs) {
      return
    }
    def vars = options.Vs as List<String>
    for (int i = 0; i < vars.size() / 2; i++) {
      h.set(vars[i * 2], vars[i * 2 + 1])
    }
  }

}
