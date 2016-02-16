package com.stanfy.helium.internal.entities

import com.stanfy.helium.DefaultType
import com.stanfy.helium.internal.utils.ConfigurableGenericMap
import com.stanfy.helium.model.DataType
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.FileType
import com.stanfy.helium.model.FormType
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.MultipartType
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type

import static com.stanfy.helium.internal.utils.DslUtils.runWithProxy

/**
 * Provides DSL for building entity of the specified type.
 * <ul>
 *   <li>Message and dictionary entities are represented as maps.</li>
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
    def byteArrayEntity = byteArrayEntityFromDataType(value)
    if (byteArrayEntity) {
      return byteArrayEntity
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
    if (type instanceof Dictionary) {
      def (value, proxy) = dictionaryBuilder()
      runWithProxy(proxy, spec)
      return value
    }

    def value = new LinkedHashMap<String, Object>()
    if (type instanceof MultipartType) {
      runWithProxy(new MultipartEntityBuilder(value, type.name, scope), spec)
    } else if (!(type instanceof Message) && !(type instanceof FormType)) {
      throw new IllegalArgumentException("Can use closure to build a value of type $type")
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

  /**
   * Returns {@link ByteArrayEntity} if type is {@link DataType}, and value is convertible to
   * <code>ByteArrayEntity</code>, e.g. is <code>byte[]</code> or <code>ByteArrayEntity</code>.
   * If it's not convertible - returns null.
   * @param value object to be converted to ByteArrayEntity
   * @return ByteArrayEntity or null if can't convert.
   */
  private ByteArrayEntity byteArrayEntityFromDataType(final Object value) {
    if (!(type instanceof DataType)) {
      return null
    }

    if (value instanceof ByteArrayEntity) {
      return value as ByteArrayEntity
    }
    if (value instanceof byte[]) {
      return new ByteArrayEntity(value)
    }
    // Value is not convertible to byte array entity.
    return null
  }

  private def dictionaryBuilder() {
    if (type.name == DefaultType.STRING.langName) {
      def map = new LinkedHashMap<String, Object>()
      return [map, new DictionaryBuilder<String>(map, type.name, String.class, scope)]
    }
    def map = new LinkedHashMap<Object, Object>()
    return [map, new DictionaryBuilder<Object>(map, type.name, String.class, scope)]
  }

  class MessageBuilder extends ConfigurableGenericMap<String, Object> {

    MessageBuilder(final Map<String, Object> map, final String name, final Map<String, Object> scope) {
      super(map, name, String.class, scope)
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

  class MultipartEntityBuilder extends ConfigurableGenericMap<String, Object> {

    MultipartEntityBuilder(final Map<String, Object> map, final String name, final Map<String, Object> scope) {
      super(map, name, String.class, scope)
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

  class DictionaryBuilder<K> extends ConfigurableGenericMap<K, Object> {

    DictionaryBuilder(Map<K, Object> map, String name, Class<K> clazz, Map<String, Object> scope) {
      super(map, name, clazz, scope)
    }

    @Override
    protected Object resolveValue(K key, Object arg) {
      Dictionary dict = type as Dictionary;
      return new TypedEntityValueBuilder(dict.value, scope).from(arg)
    }

    void entry(Object key, Object value) {
      Dictionary dict = type as Dictionary;
      K mapKey = new TypedEntityValueBuilder(dict.key, scope).from(key) as K
      def mapValue = new TypedEntityValueBuilder(dict.value, scope).from(value)
      map[mapKey] = mapValue
    }

  }

}
