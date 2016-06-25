package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

/**
 * Created by paultaykalo on 6/25/16.
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