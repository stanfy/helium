package com.stanfy.helium.dsl

import com.stanfy.helium.model.TypeResolver
import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.model.*
import groovy.transform.PackageScope

import static com.stanfy.helium.utils.DslUtils.runWithProxy;

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
  Service serviceByName(final String name) {
    return services.find() { it.name == name }
  }

  @Override
  List<Service> getServices() {
    return Collections.unmodifiableList(services)
  }

  @Override
  List<Message> getMessages() {
    applyPendingTypes()
    return Collections.unmodifiableList(messages)
  }

  @Override
  TypeResolver getTypes() {
    applyPendingTypes()
    return typeResolver
  }

  @Override
  List<Note> getNotes() {
    applyPendingTypes()
    return Collections.unmodifiableList(notes)
  }

  @Override
  List<StructureUnit> getStructure() {
    applyPendingTypes()
    return Collections.unmodifiableList(structure)
  }

  @Override
  List<Sequence> getSequences() {
    applyPendingTypes()
    return Collections.unmodifiableList(sequences)
  }

  @PackageScope
  TypeResolver getTypeResolver() { return typeResolver }

  public Message createAndAddMessage(final String name, final Closure<?> spec, final boolean addToStructure) {
    Message m = new Message(name : name)
    runWithProxy(new FieldsBuilder(m, this, typeResolver), spec)
    messages.add m
    updatePendingTypes(name, m, addToStructure)
    return m
  }

  public Sequence createAndAddSequence(final String name, final String itemsType) {
    Sequence seq = new Sequence(name : name, itemsType : typeResolver.byName(itemsType))
    sequences.add seq
    updatePendingTypes(name, seq, true)
    return seq
  }

  private void updatePendingTypes(final String name, final Type type, final boolean addToStructure) {
    Type prevType = pendingTypeDefinitions.remove(name)
    pendingTypeDefinitions[name] = type
    if (addToStructure) {
      if (prevType) {
        structure.set(structure.indexOf(prevType), type)
      } else {
        structure.add(type)
      }
    }
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
    runWithProxy(new ConfigurableService(service, this), description)
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
        "message" : { Closure<?> spec -> createAndAddMessage(name, spec, true) },
        "spec" : { Closure<?> spec -> runWithProxy(new ConfigurableProxy<Type>(type, owner), spec) },
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
