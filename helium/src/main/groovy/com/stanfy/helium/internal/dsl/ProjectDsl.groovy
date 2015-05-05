package com.stanfy.helium.internal.dsl

import com.stanfy.helium.handler.ScriptExtender
import com.stanfy.helium.internal.model.tests.BehaviorDescriptionContainer
import com.stanfy.helium.internal.model.tests.BehaviourDescription
import com.stanfy.helium.internal.model.tests.CheckGroup
import com.stanfy.helium.internal.model.tests.CheckableService
import com.stanfy.helium.model.*
import com.stanfy.helium.model.tests.BehaviourSuite
import groovy.transform.PackageScope

import java.nio.charset.Charset

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Entry point to Helium DSL.
 */
class ProjectDsl implements Project, BehaviorDescriptionContainer {

  /** Services list. */
  private final List<Service> services = new ArrayList<>()
  /** Messages list. */
  private final List<Message> messages = new ArrayList<>()
  /** Sequences list. */
  private final List<Sequence> sequences = new ArrayList<>()
  /** Notes list. */
  private final List<Note> notes = new ArrayList<>()
  /** Included files list. */
  private final List<File> includedFiles = new ArrayList<>()

  /** Behaviour specs. */
  private final List<BehaviourDescription> behaviourDescriptions = new ArrayList<>()

  /** Structure. */
  private final List<StructureUnit> structure = new ArrayList<>()

  /** Pending type definitions. */
  private final LinkedHashMap<String, Type> pendingTypeDefinitions = new LinkedHashMap<>()

  /** Types resolver. */
  private TypeResolver typeResolver = new DefaultTypeResolver()

  /** Used charset. */
  private Charset charset = Charset.forName("UTF-8")

  final Binding variablesBinding = new Binding()

  @Override
  List<File> getIncludedFiles() {
    return includedFiles
  }

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

  public void updateMessageParent(final Message msg, final String parentName) {
    if (msg.name == parentName) {
      throw new IllegalArgumentException("Bad type: " + msg.name + ". Message cannot be parent of itself.")
    }
    def parent = types.byName(parentName)
    if (!(parent instanceof Message)) {
      throw new IllegalArgumentException("Bad type: " + msg.name + ". Only messages can be parents of messages.")
    }
    msg.parent = parent as Message
  }

  public Sequence createAndAddSequence(final String name, final String itemsType) {
    Sequence seq = new Sequence(name : name, itemsType : typeResolver.byName(itemsType))
    sequences.add seq
    updatePendingTypes(name, seq, true)
    return seq
  }

  public void updatePrimitiveType(final Type type) {
    Type prevType = pendingTypeDefinitions.put(type.name, type)
    if (!prevType) {
      throw new IllegalStateException("Type $type.name is not in pending type definitions")
    }
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

  @Override
  void addBehaviourDescription(final BehaviourDescription d) {
    behaviourDescriptions.add d
  }

  int checksCount() {
    return behaviourDescriptions.size()
  }

  @Override
  BehaviourSuite check() {
    return new CheckGroup(behaviourDescriptions).run("Project checks")
  }

  // -------- DSL methods --------

  public void service(final Closure<?> description) {
    applyPendingTypes()
    CheckableService service = new CheckableService()
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
    return TypeDsl.create(type, this)
  }

  public void note(final String text) {
    applyPendingTypes()
    Note note = new Note(value: text)
    notes.add note
    structure.add note
  }

  public void include(final Object spec) {
    final File specFile
    if (spec instanceof File) {
      specFile = spec as File
    } else {
      def path = spec as String
      if (path.startsWith("file:") || path.startsWith("jar:")) {
        specFile = new File(new URI(path))
      } else {
        specFile = new File(path)
      }
    }
    includedFiles.add specFile
    ScriptExtender.fromFile(specFile, charset).withVars(variablesBinding).handle(this)
  }

  BehaviourDescriptionBuilder describe(final String name) {
    return new BehaviourDescriptionBuilder(name, this)
  }

}
