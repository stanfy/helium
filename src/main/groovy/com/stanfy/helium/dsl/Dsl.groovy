package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.Type
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

  /** Pending type defenitions. */
  private final LinkedHashMap<String, Type> pendingTypeDefinitions = new LinkedHashMap<>()

  /** Types resolver. */
  private TypeResolver typeResolver = new DefaultTypeResolver()

  public List<Service> getServices() {
    return Collections.unmodifiableList(services)
  }

  public List<Message> getMessages() {
    applyPendingTypes()
    return Collections.unmodifiableList(messages)
  }

  public TypeResolver getTypes() {
    applyPendingTypes()
    return typeResolver
  }

  public Message createAndAddMessage(final String name, Closure<?> spec) {
    spec = (Closure<?>) spec.clone()
    Message m = new Message(name : name)
    messages.add m
    spec.delegate = new FieldsBuilder(m, this, typeResolver)
    spec.resolveStrategy = Closure.DELEGATE_ONLY
    spec.call()
    pendingTypeDefinitions[name]= m
    return m
  }

  private void applyPendingTypes() {
    pendingTypeDefinitions.values().each { Type type ->
      typeResolver.registerNewType type
    }
    pendingTypeDefinitions.clear()
  }

  // -------- DSL methods --------

  public void service(final Closure<?> description) {
    applyPendingTypes()

    Service service = new Service()
    new ConfigurableService(service, this).configure description
    services.add service
  }

  public def type(final Object arg) {
    applyPendingTypes()
    String name = "$arg"
    pendingTypeDefinitions[name] = new Type(name : name)
    return [
        "message" : { Closure<?> spec -> createAndAddMessage(name, spec) }
    ]
  }

}
