package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
class MessageHierarchy {

  public static final String PREFIX_CYCLE_DEPENDENCIES = "Cyclic message hierarchy: "
  public static final String PREFIX_PARENT_TYPE_NOT_FOUND = "Message set does contain type "

  private Set<String> externalParentClasses = new HashSet<>()


  void setExternalParentClasses(final Set<String> externalParentClasses) {
    this.externalParentClasses = externalParentClasses
  }

  void buildAndValidate(final Collection<Message> input) {
    Map<String, List<Message>> map = new HashMap<>();
    def validNames = new HashSet<String>()
    input.collect {validNames.add it.name}
    def roots = new LinkedHashSet<Message>()

    validNames.addAll externalParentClasses
    // add all to index, and fill out roots
    input.each { msg ->
      def key = "";
      if (msg.hasParent()) {
        if (!validNames.contains(msg.parent)) {
          throw new IllegalArgumentException(PREFIX_PARENT_TYPE_NOT_FOUND + msg.parent)
        }

        key = externalParentClasses.contains(msg.parent) ? "" : msg.parent
      }
      if (key == "") {
        roots.add msg
        return
      }
      def value = map.containsKey(key) ? map[key] : new ArrayList<>()
      value.add msg

      map[key] = value
    }

    if (roots.isEmpty()) {
      throw new IllegalArgumentException(PREFIX_CYCLE_DEPENDENCIES + findCycleToString(map))
    }

    def parents = new ArrayList<Message>()
    def next = new ArrayList<Message>()
    parents.addAll roots

    while (!parents.isEmpty()) {
      next.clear()

      parents.each {
        if (map.containsKey(it.name)) {
          next.addAll map.remove(it.name)
        }
      }
      parents = next
    }

    if (!map.isEmpty()) {
      throw new IllegalArgumentException(PREFIX_CYCLE_DEPENDENCIES + findCycleToString(map))
    }

  }

  static String findCycleToString(final Map<String, List<Message>> map) {
    def cycle = new LinkedHashSet<String>()
    def visited = new HashSet<String>()

    def firstParent = map.keySet().first()

    if (findNext(firstParent, map, cycle, visited)) {
      return cycleToString(cycle)
    } else {
      return ""
    }

  }

  static String cycleToString(final Collection<String> cycle) {
    return cycle.join(" -> ")
  }

  static boolean findNext(
      final String name,
      final Map<String, List<Message>> map,
      final LinkedHashSet<String> cycle,
      final Set<String> visited) {

    if (!map.containsKey(name)) {
      cycle.clear()
      return false
    }
    cycle.add name
    if (visited.contains(name)) {
      return true
    }
    visited.add name
    for (msg in map[name]) {
      def childCycle = findNext(msg.name, map, cycle, visited)
      if (childCycle) {
        return true
      }
    }

    return false
  }

}
