package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import spock.lang.Specification

import java.lang.reflect.Array

/**
 * Created by paultaykalo on 6/25/16.
 */
class SwiftFilesGeneratorTest extends Specification {

  List<SwiftFile> files
  File tmpDir
  SwiftFilesGenerator sut

  def setup() {
    files = [
    new SwiftFile(){
      String name() { return "A" }
      String contents() { return "Example" }
    },
    new SwiftFile(){
      String name() { return "B" }
      String contents() { return "Example2" }
    }]

    tmpDir = File.createTempDir()
  }

  def tearDown() {
    tmpDir.delete()
  }

  def "Generate"() {
    when:
    sut = new SwiftFilesGenerator()
    sut.generate(tmpDir, files)

    then:
    new File("$tmpDir/A.swift").exists()
    new File("$tmpDir/B.swift").exists()
  }
}
