package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.internal.SourceCodeGenerators
import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import com.stanfy.helium.utils.DslUtils
import groovy.transform.PackageScope

import static com.stanfy.helium.gradle.UserConfig.specName

/**
 * Delegate for DSL
 * <code>
 *   sourceGen {
 *     entities {
 *     }
 *     constants {
 *     }
 *   }
 * </code>
 */
class SourceGenDslDelegate {

  static {
    def meta = SourceGenDslDelegate.metaClass
    SourceCodeGenerators.GENERATORS.each { String name, def params ->
      // configuration method
      meta."$name" << { Closure<?> config ->
        setDelegate(name, new GeneratorDslDelegate(genOptions: params.optionsFactory()))
        DslUtils.runWithProxy(getDelegate(name), config)
      }
    }
  }

  private final Map<String, GeneratorDslDelegate> delegatesMap = [:]

  private final Object owner

  public SourceGenDslDelegate(final Object owner) {
    this.owner = owner
  }

  @SuppressWarnings("GrMethodMayBeStatic")
  @PackageScope
  Collection<String> allGenerators() {
    return SourceCodeGenerators.GENERATORS.keySet()
  }

  @PackageScope
  GeneratorDslDelegate getDelegate(final String name) {
    return delegatesMap[name]
  }

  @PackageScope
  def setDelegate(final String name, final GeneratorDslDelegate generator) {
    delegatesMap[name] = generator
  }

  @PackageScope
  void createTasks(final UserConfig userConfig, final File specification, final URL[] classpath,
                   final String basePath, final HeliumExtension config) {
    delegatesMap.each { String name, GeneratorDslDelegate delegate ->
      if (!delegate) {
        return
      }
      if (!delegate.output) {
        delegate.output = new File(userConfig.project.buildDir, "$basePath/$name/${specName(specification)}")
      }
      def task = userConfig.project.tasks.create(
          HeliumInitializer.taskName("generate${name.capitalize()}", specification, config),
          SourceCodeGenerators.GENERATORS[name].task as Class<? extends BaseHeliumTask>
      )
      HeliumInitializer.configureHeliumTask(task, specification, delegate.output, classpath, userConfig)
      task.options = delegate.genOptions
      config.sourceGen(specification)[name] = task
    }
  }

  static final class GeneratorDslDelegate {

    def genOptions

    File output

    void output(final File output) {
      this.output = output;
    }

    void options(final Closure<?> config) {
      DslUtils.runWithProxy(genOptions, config)
    }

  }

}
