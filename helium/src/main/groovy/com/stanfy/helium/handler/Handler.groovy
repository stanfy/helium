package com.stanfy.helium.handler

import com.stanfy.helium.model.Project

/**
 * DSL handler.
 */
interface Handler {

  void handle(Project project)

}
