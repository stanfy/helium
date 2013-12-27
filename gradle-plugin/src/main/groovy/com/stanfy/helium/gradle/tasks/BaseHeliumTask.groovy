package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.Helium
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory

/**
 * Created by roman on 12/27/13.
 */
class BaseHeliumTask extends DefaultTask {

  /** Helium instance. */
  Helium helium

  /** Input specification file. */
  @InputFile
  File input

  /** Output directory. */
  @OutputDirectory
  File output

}
