package com.stanfy.helium.entities

import com.stanfy.helium.model.DataType
import com.stanfy.helium.model.FileType
import com.stanfy.helium.model.FormType
import com.stanfy.helium.model.MultipartType
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

  final Map<String, Object> scope;

  public TypedEntityValueBuilder(final Type type) {
    this(type, Collections.emptyMap())
  }
  public TypedEntityValueBuilder(final Type type, final Map<String, Object> scope) {
    this.type = type
    this.scope = scope
  }

  def from(final Object value) {
    if (type instanceof DataType) {
      if (value instanceof ByteArrayEntity) {
        return value as ByteArrayEntity
      } else if (value instanceof byte[]) {
        return new ByteArrayEntity(value)
      }
    }
    if (type instanceof FileType && value instanceof File) {
      return value as File
    }
    if (value instanceof Closure) {
      return from((Closure<?>)value)
    }
    if (value instanceof Collection) {
      return from((Collection<?>)value)
    }

    if (!type.primitive) {
      throw new IllegalArgumentException("Value $value of type ${value.class} can be used to build a primitive type entity only, not the $type")
    }
    return value
  }

  def from(final Closure<?> spec) {
    def value = new LinkedHashMap<String, Object>()
    if (type instanceof MultipartType) {
      runWithProxy(new MultipartEntityBuilder(value, type.name, scope), spec)
    } else if (!(type instanceof Message) && !(type instanceof FormType)) {
      throw new IllegalArgumentException("Can use closure to build messages or forms only, not the $type")
    } else {
      runWithProxy(new MessageBuilder(value, type.name, scope), spec)
    }
    return value
  }

  def from(final Collection<?> list) {
    if (!(type instanceof Sequence)) {
      throw new IllegalArgumentException("Can use lists to build sequences only, not the $type")
    }
    Sequence seq = (Sequence) type
    return buildListValue(seq.itemsType, list)
  }

  protected def buildListValue(final Type itemType, final Collection<?> list) {
    def value = []
    TypedEntityValueBuilder itemBuilder = new TypedEntityValueBuilder(itemType, scope)
    list.each {
      value += itemBuilder.from(it)
    }
    return value
  }

  class MessageBuilder extends ConfigurableMap<Object> {

    MessageBuilder(final Map<String, Object> map, final String name, final Map<String, Object> scope) {
      super(map, name, scope)
    }

    @Override
    protected Object resolveValue(final String key, final Object arg) {
      Message msg
      String msgOrForm
      if (type instanceof FormType) {
        msg = (type as FormType).base
        msgOrForm = "form"
      } else {
        msg = type as Message
        msgOrForm = "message ${msg.name}"
      }

      Field field = msg.fieldByName(key)
      if (!field) {
        throw new IllegalArgumentException("Unknown field $key in $msgOrForm")
      }
      if (field.sequence) {
        if (!(arg instanceof Collection)) {
          throw new IllegalArgumentException("Collection expected for field $key in $msgOrForm")
        }
        return buildListValue(field.type, (Collection<?>)arg)
      }
      return new TypedEntityValueBuilder(field.type, scope).from(arg)
    }
  }

  class MultipartEntityBuilder extends ConfigurableMap<Object> {

    MultipartEntityBuilder(
        final Map<String, Object> map, final String name, final Map<String, Object> scope) {
      super(map, name, scope)
    }

    @Override
    protected Object resolveValue(final String key, final Object arg) {
      MultipartType multipartType = type as MultipartType
      if (multipartType.isGeneric()) {
        return arg
      }
      if (!multipartType.parts.containsKey(key)) {
        throw new IllegalArgumentException("Unknown part $key in multipart form")
      }
      Type partType = multipartType.parts.get(key)
      return new TypedEntityValueBuilder(partType, scope).from(arg)
    }
  }

}
