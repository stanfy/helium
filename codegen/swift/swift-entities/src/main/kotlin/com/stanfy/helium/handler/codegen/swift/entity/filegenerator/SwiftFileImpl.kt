package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

data class SwiftFileImpl(val name: String, val contents: String) : SwiftFile {
  override fun name(): String {
    return name
  }

  override fun contents(): String {
    return contents;
  }
}