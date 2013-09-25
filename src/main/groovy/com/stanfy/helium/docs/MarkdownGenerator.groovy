package com.stanfy.helium.docs

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.StructureUnit

/**
 * Generates markdown file from the project.
 */
class MarkdownGenerator implements Handler {

  /** Output. */
  private final Writer out

  public MarkdownGenerator(final Writer out) {
    this.out = out
  }
  public MarkdownGenerator(final File out, final String encoding) {
    this.out = new OutputStreamWriter(new FileOutputStream(out), encoding)
  }

  @Override
  void handle(final Project project) {
    try {
      project.structure.each { StructureUnit unit ->

      }
    } finally {
      out.close()
    }
  }

}
