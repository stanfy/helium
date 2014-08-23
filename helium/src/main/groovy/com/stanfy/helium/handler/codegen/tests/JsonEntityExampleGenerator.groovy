package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.handler.codegen.java.JavaPrimitiveTypes
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.json.JsonBuilder
import groovy.transform.PackageScope

/**
 * Generates JSON object based on given type examples.
 * TODO: use entity builder + writer
 */
@PackageScope
class JsonEntityExampleGenerator {

  /** Types resolver. */
  private final TypeResolver typeResolver

  public JsonEntityExampleGenerator(final TypeResolver typeResolver) {
    this.typeResolver = typeResolver
  }

  JsonBuilder generate(final Type type) throws NoExamplesProvidedException {
    JsonBuilder json = new JsonBuilder()
    if (type instanceof Sequence) {
      json([generate(type.itemsType)])
      return json
    }

    if (type instanceof Message) {
      Message msg = (Message) type
      json {
        msg.fields.each { Field field ->
          if (field.type instanceof Sequence || field.type instanceof Message) {

            "$field.name" generate(field.type)

          } else {

            if (!field.examples && field.required) {
              throw new NoExamplesProvidedException(field, "In type $msg")
            }

            if (field.examples) {
              "$field.name" cast(field.examples[0], field.type)
            }

          }

        }
      }
      return json
    }

    throw new UnsupportedOperationException("Cannot generate JSON from $type")
  }

  private static def cast(final Object example, final Type type) {
    Class<?> groovyClass = JavaPrimitiveTypes.javaClass(type)
    return example.asType(groovyClass)
  }

}
