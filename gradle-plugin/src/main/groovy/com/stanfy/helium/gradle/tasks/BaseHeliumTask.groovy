package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.Helium
import com.stanfy.helium.internal.dsl.HeliumScript
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Created by roman on 12/27/13.
 */
abstract class BaseHeliumTask<T> extends DefaultTask {

  /** Input specification file. */
  @InputFile
  File input

  /** Output directory. */
  @OutputDirectory
  File output

  /** Variables binding. */
  @Input
  Map<String, String> variables

  URL[] classpath

  /** Handler options. */
  T options

  private Helium heliumInstance

  protected Helium getHelium() {
    if (!heliumInstance) {
      heliumInstance = new Helium().defaultTypes()
      if (!variables.isEmpty()) {
        variables.each { String name, String value ->
          helium.set name, value
        }
      }
      if (input) {
        File baseDir = input.parentFile
        heliumInstance.set "baseDir", baseDir from input
      }
    }
    return heliumInstance
  }

  protected abstract void doIt();

  @TaskAction
  final void runWithClassLoader() {
    ClassLoader oldClassLoader = Thread.currentThread().contextClassLoader
    URLClassLoader classLoader = new URLClassLoader(classpath, HeliumScript.classLoader)
    Thread.currentThread().setContextClassLoader(classLoader)
    try {
      doIt()
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader)
    }
  }

}
