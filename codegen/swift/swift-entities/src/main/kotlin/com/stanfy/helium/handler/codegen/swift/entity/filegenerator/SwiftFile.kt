package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

/**
 * Generic interface for all swift files
 */
interface SwiftFile {
  /**
   * file name
   */
  fun name(): String

  /**
   * File contents
   */
  fun contents(): String
}