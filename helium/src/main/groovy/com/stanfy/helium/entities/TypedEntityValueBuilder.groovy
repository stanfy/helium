package com.stanfy.helium.entities

import com.stanfy.helium.utils.ConfigurableMap
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import static com.stanfy.helium.utils.DslUtils.runWithProxy;


/**
 * Provides DSL for building entity of the specified type.
 * <ul>
 *   <li>Message entities are represented as maps.</li>
 *   <li>Sequence entities are represented as lists.</li>
 *   <li>Primitive type entities are represented as passed objects.</li>
 * </ul>
 */
class TypedEntityValueBuilder {

  /** Entity type. */
  final Type type

  public TypedEntityValueBuilder(final Type type) {
    this.type = type
  }

  def from(final Object value) {
    if (!type.primitive) {
      throw new IllegalArgumentException("Value $value of type ${value.class} can be used to build a primitive type entity only, not the $type")
    }
    return value
  }

  def from(final Closure<?> spec) {
    if (!(type instanceof Message)) {
      throw new IllegalArgumentException("Can use closure to build messages only, not the $type")
    }
    def value = new LinkedHashMap<String, Object>()
    runWithProxy(new MessageBuilder(value, type.name), spec)
    return value
  }

  def from(final Collection<?> list) {
    if (!(type instanceof Sequence)) {
      throw new IllegalArgumentException("Can use lists to build sequences only, not the $type")
    }
    Sequence seq = (Sequence) type
    return buildListValue(seq.itemsType, list)
  }

  protected static def buildListValue(final Type itemType, final Collection<?> list) {
    def value = []
    TypedEntityValueBuilder itemBuilder = new TypedEntityValueBuilder(itemType)
    list.each {
      value += itemBuilder.from(it)
    }
    return value
  }

  class MessageBuilder extends ConfigurableMap<Object> {

    MessageBuilder(final Map<String, Object> map, final String name) {
      super(map, name)
    }

    @Override
    protected Object resolveValue(final String key, final Object arg) {
      Message msg = (Message) type
      Field field = msg.fieldByName(key)
      if (!field) {
        throw new IllegalArgumentException("Unknown field $key in message $msg")
      }
      if (field.sequence) {
        if (!(arg instanceof Collection)) {
          throw new IllegalArgumentException("Collection expected for field $key in message $msg")
        }
        return buildListValue(field.type, (Collection<?>)arg)
      }
      return new TypedEntityValueBuilder(field.type).from(arg)
    }
  }

}
