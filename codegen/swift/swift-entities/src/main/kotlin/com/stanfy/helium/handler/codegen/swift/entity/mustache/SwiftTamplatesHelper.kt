package com.stanfy.helium.handler.codegen.swift.entity.mustache

import com.github.mustachejava.DefaultMustacheFactory
import java.io.StringWriter

/**
 * Created by paultaykalo on 6/25/16.
 */
class SwiftTamplatesHelper {

  companion object {
    fun generateSwiftStruct(name: String): String {
      return generatedTemplateWithName("SwiftStruct.mustache", object : Any () {
        val name: String = name
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