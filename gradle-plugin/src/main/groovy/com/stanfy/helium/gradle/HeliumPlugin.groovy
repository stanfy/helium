package com.stanfy.helium.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import java.lang.reflect.Constructor

/**
 * Gradle plugin for Helium.
 */
class HeliumPlugin implements Plugin<Project> {

  private Config config

  @Override
  void apply(final Project project) {
    project.extensions.add("helium", HeliumExtension)
    HeliumExtension hel = project.helium
    config = new Config(project)
    hel.attach config

    project.afterEvaluate {
      createTasks(project)
    }
  }

  void createTasks(final Project project) {
    HeliumExtension extension = project.helium
    createInitializer(
        extension.classpath ? extension.classpath : project.files(), extension
    ).createTasks()
  }

  private TasksCreator createInitializer(final FileCollection classpath, final HeliumExtension ext) {
    URL[] urls = classpath.collect() { it.toURI().toURL() } as URL[]
    URLClassLoader classLoader = new URLClassLoader(urls, HeliumPlugin.classLoader)
    Class<?> creatorClass = classLoader.loadClass(HeliumPlugin.package.name + ".HeliumInitializer")
    Constructor<?> constructor = creatorClass.getDeclaredConstructor(HeliumExtension.class, Config.class)
    constructor.setAccessible(true)
    return constructor.newInstance(ext, config) as TasksCreator
  }

}
