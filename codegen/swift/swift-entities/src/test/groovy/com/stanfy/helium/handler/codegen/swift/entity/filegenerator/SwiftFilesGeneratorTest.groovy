package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import spock.lang.Specification

class SwiftFilesGeneratorTest extends Specification {

  List<SwiftFile> files
  File tmpDir
  SwiftOutputGenerator sut

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

  def "should generate specified files"() {
    when:
    sut = new SwiftOutputGeneratorImpl()
    sut.generate(tmpDir, files)

    then:
    new File("$tmpDir/A.swift").exists()
    new File("$tmpDir/B.swift").exists()
  }
}
