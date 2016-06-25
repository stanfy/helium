package com.stanfy.helium.handler.codegen.swift.entity.mustache

import com.github.mustachejava.DefaultMustacheFactory
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnumCase
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftProperty
import java.io.StringWriter

class SwiftTemplatesHelper {

  companion object {
    fun generateSwiftStruct(name: String, properties: List<SwiftProperty>): String {
      return generatedTemplateWithName("SwiftStruct.mustache", object : Any () {
        val name = name
        val props = properties
      })
    }

    fun generateSwiftEnum(name: String, values: List<SwiftEntityEnumCase>): String {
      return generatedTemplateWithName("SwiftEnum.mustache", object : Any () {
        val name = name
        val values = values
      })
    }

    fun generateSwiftTypeAlias(name: String, itemType: SwiftEntity): Any {
      return generatedTemplateWithName("SwiftTypeAlias.mustache", object : Any () {
        val name = name
        val type = itemType.typeString()
      })
    }


    fun generatedTemplateWithName(templateName: String, templateObject: Any): String {
      val mustacheFactory = DefaultMustacheFactory()
      val mustache = mustacheFactory.compile(templateName)
      val stringWriter = StringWriter()
      mustache.execute(stringWriter, templateObject)
      return stringWriter.toString()
    }

  }

}