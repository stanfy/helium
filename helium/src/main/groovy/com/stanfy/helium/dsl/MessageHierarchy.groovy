package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
class MessageHierarchy {


  public static final String PREFIX_CYCLE_DEPENDENCIES = "Cyclic message hierarchy: "
  public static final String PREFIX_PARENT_TYPE_NOT_FOUND = "Message set does contain type "

  void buildAndValidate(final Collection<Message> input) {
    Map<String, Node> map = new HashMap<>();

    // add all to index
    input.each { map[it.name] = new Node(it) }

    map.values().each { n ->
      if (n.msg.hasParent()) {
        def parentName = n.msg.parent
        if (!map.containsKey(parentName)) {
          // TODO add check for base custom classes here
          throw new IllegalArgumentException(PREFIX_PARENT_TYPE_NOT_FOUND + parentName)
        }

        map[parentName].addChild(n)
      }
    }

    def roots = map.values()

    // DFS to look for cycles
    def cycle = findCycle(roots)
    if (!cycle.isEmpty()) {
      throw new IllegalArgumentException(PREFIX_CYCLE_DEPENDENCIES + cycleToString(cycle))
    }

  }

  static String cycleToString(final Set<Node> nodes) {
    nodes.msg.name.join(' -> ')
  }

  Set<Node> findCycle(final Collection<Node> nodes) {
    Set<Node> cycle = new LinkedHashSet<>();
    for (node in nodes) {
      if (node.visited) {
        // build parent cycle
        cycle << node

        def parentNode = node.parentNode
        while (parentNode != node) {
          cycle.add parentNode
          parentNode = parentNode.parentNode
        }
        return cycle
      }

      node.visited = true

      def childCycle = findCycle(node.children)
      if (!childCycle.isEmpty()) {
        // cycle found - do not continue
        return childCycle
      }
    }
    // cycle not found
    return []
  }

  static class Node {
    Message msg;
    Set<Node> children = new HashSet<>();
    Node parentNode;

    boolean visited;

    Node(final Message msg) {
      this.msg = msg
    }

    def addChild(final Node node) {
      children << node
      node.parentNode = this
    }
  }
}
