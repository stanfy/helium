package com.stanfy.helium.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Spec for HeliumPlugin.
 */
class HeliumPluginSpec extends Specification {

  def "creates genApiTests task"() {
    given:
    Project p = ProjectBuilder.builder().build()
    p.apply plugin: 'helium'
    File f = File.createTempFile("abc", "abc")
    f.deleteOnExit()
    f << "type 'string'"
    p.helium {
      specification f
    }

    (p.plugins.withType(HeliumPlugin).collect() as List)[0].createTasks(p)

    expect:
    p.tasks.findByName('genApiTests') != null
    p.tasks.findByName('runApiTests') != null
  }

}
