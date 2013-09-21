package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Service
import groovy.transform.CompileStatic

/**
 * Entry point to Helium DSL.
 */
@CompileStatic
class Dsl {

  /** Services list. */
  private final List<Service> services = new ArrayList<>()
  /** Messages list. */
  private final List<Message> messages = new ArrayList<>()

  /** Types resolver. */
  private TypeResolver typeResolver = new DefaultTypeResolver()

  public List<Service> getServices() {
    return Collections.unmodifiableList(services)
  }

  public List<Message> getMessages() {
    return Collections.unmodifiableList(messages)
  }

  private Message createAndAddMessage(final String name, final Closure<?> spec) {
    Message m = new Message(
        name : name
    )
    messages.add m
    spec.delegate = new FieldsBuilder(m, typeResolver)
    spec.resolveStrategy = Closure.DELEGATE_FIRST
    spec.call()
    return m
  }

  // -------- DSL methods --------

  public void service(final Closure<?> description) {
    Service service = new Service()
    service.configure description
    services.add service
  }

  public def message(final String name) {
    return [
        "spec" : { Closure<?> spec -> createAndAddMessage(name, spec) }
    ]
  }

}
