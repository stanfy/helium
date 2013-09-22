package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Note
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.StructureUnit
import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Entry point to Helium DSL.
 */
@CompileStatic
class Dsl {

  /** Services list. */
  private final List<Service> services = new ArrayList<>()
  /** Messages list. */
  private final List<Message> messages = new ArrayList<>()
  /** Notes list. */
  private final List<Note> notes = new ArrayList<>()

  /** Structure. */
  private final List<StructureUnit> structure = new ArrayList<>()

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

  public List<Note> getNotes() {
    applyPendingTypes()
    return Collections.unmodifiableList(notes)
  }

  public List<StructureUnit> getStructure() {
    applyPendingTypes()
    return Collections.unmodifiableList(structure)
  }

  public Message createAndAddMessage(final String name, final Closure<?> spec) {
    Message m = new Message(name : name)
    callConfigurationSpec(new FieldsBuilder(m, this, typeResolver), spec)
    messages.add m
    Type prevType = pendingTypeDefinitions.remove(name)
    pendingTypeDefinitions[name] = m
    if (prevType) {
      structure.set(structure.indexOf(prevType), m)
    } else {
      structure.add(m)
    }
    return m
  }

  @PackageScope static void callConfigurationSpec(final def proxy, final Closure<?> spec) {
    Closure<?> body = spec.clone() as Closure<?>
    body.resolveStrategy = Closure.DELEGATE_ONLY
    body.delegate = proxy
    body.call()
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
    callConfigurationSpec(new ConfigurableService(service, this), description)
    services.add service
    structure.add service
  }

  public def type(final Object arg) {
    applyPendingTypes()
    String name = "$arg"
    Type type = new Type(name : name)
    pendingTypeDefinitions[name] = type
    structure.add type
    return [
        "message" : { Closure<?> spec -> createAndAddMessage(name, spec) },
        "spec" : { Closure<?> spec -> callConfigurationSpec(new ConfigurableProxy<Type>(type, owner), spec) }
    ]
  }

  public void note(final String text) {
    applyPendingTypes()
    Note note = new Note(value: text)
    notes.add note
    structure.add note
  }

}
