package com.stanfy.helium.dsl

import com.stanfy.helium.model.*
import groovy.transform.PackageScope

/**
 * Entry point to Helium DSL.
 */
class ProjectDsl implements Project {

  /** Services list. */
  private final List<Service> services = new ArrayList<>()
  /** Messages list. */
  private final List<Message> messages = new ArrayList<>()
  /** Sequences list. */
  private final List<Sequence> sequences = new ArrayList<>()
  /** Notes list. */
  private final List<Note> notes = new ArrayList<>()

  /** Structure. */
  private final List<StructureUnit> structure = new ArrayList<>()

  /** Pending type defenitions. */
  private final LinkedHashMap<String, Type> pendingTypeDefinitions = new LinkedHashMap<>()

  /** Types resolver. */
  private TypeResolver typeResolver = new DefaultTypeResolver()

  @Override
  public List<Service> getServices() {
    return Collections.unmodifiableList(services)
  }

  @Override
  public List<Message> getMessages() {
    applyPendingTypes()
    return Collections.unmodifiableList(messages)
  }

  @Override
  public TypeResolver getTypes() {
    applyPendingTypes()
    return typeResolver
  }

  @Override
  public List<Note> getNotes() {
    applyPendingTypes()
    return Collections.unmodifiableList(notes)
  }

  @Override
  public List<StructureUnit> getStructure() {
    applyPendingTypes()
    return Collections.unmodifiableList(structure)
  }

  @Override
  public List<Sequence> getSequences() {
    applyPendingTypes()
    return Collections.unmodifiableList(sequences)
  }

  public Message createAndAddMessage(final String name, final Closure<?> spec) {
    Message m = new Message(name : name)
    callConfigurationSpec(new FieldsBuilder(m, this, typeResolver), spec)
    messages.add m
    updatePendingTypes(name, m)
    return m
  }

  public Sequence createAndAddSequence(final String name, final String itemsType) {
    Sequence seq = new Sequence(name : name, itemsType : typeResolver.byName(itemsType))
    sequences.add seq
    updatePendingTypes(name, seq)
  }

  private void updatePendingTypes(final String name, final Type type) {
    Type prevType = pendingTypeDefinitions.remove(name)
    pendingTypeDefinitions[name] = type
    if (prevType) {
      structure.set(structure.indexOf(prevType), type)
    } else {
      structure.add(type)
    }
  }

  @PackageScope static void callConfigurationSpec(final def proxy, final Closure<?> spec) {
    Closure<?> body = spec.clone() as Closure<?>
    body.resolveStrategy = Closure.DELEGATE_FIRST
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
        "spec" : { Closure<?> spec -> callConfigurationSpec(new ConfigurableProxy<Type>(type, owner), spec) },
        "sequence" : { String item -> createAndAddSequence(name, item) }
    ]
  }

  public void note(final String text) {
    applyPendingTypes()
    Note note = new Note(value: text)
    notes.add note
    structure.add note
  }

}
