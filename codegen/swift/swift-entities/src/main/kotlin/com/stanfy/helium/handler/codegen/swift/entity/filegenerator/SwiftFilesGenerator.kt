package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer

/**
 * Created by paultaykalo on 6/25/16.
 */
class SwiftFilesGenerator() {
  fun generate(directory: File, files: List<SwiftFile>) {
    files.forEach { swiftFile ->
      writeFile(
          File(directory, swiftFile.name() + ".swift"),
          swiftFile.contents())
    }

  }
  private fun writeFile(file: File, contents: String) {
    var output: Writer? = null
    try {
      output = OutputStreamWriter(FileOutputStream(file), "UTF-8");
      output.write(contents);
    } catch (e: RuntimeException) {
      throw RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }
}
