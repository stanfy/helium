package com.stanfy.helium.internal.utils

/** Helper for maps builder. */
abstract class ConfigurableGenericMap<K, V> extends ScopedProxy {

  /** Core map. */
  protected final Map<K, V> map

  /** Name used to describe errors. */
  protected final String name

  private final Class<K> keyClass

  protected ConfigurableGenericMap(final Map<K, V> map, final String name, final Class<K> keyClass) {
    this(map, name, keyClass, Collections.<String, Object>emptyMap())
  }

  protected ConfigurableGenericMap(final Map<K, V> map, final String name, final Class<K> keyClass,
                                   final Map<String, Object> scope) {
    super(scope)
    this.map = map
    this.name = name
    this.keyClass = keyClass
  }

  protected abstract V resolveValue(final K key, final Object arg)

  @Override
  Object invokeMethod(final String name, final Object args) {
    if (keyClass == Object.class) {
      throw new IllegalStateException(syntaxMessage(name))
    }
    final K key
    try {
      key = name.asType(keyClass)
    } catch (ClassCastException e) {
      throw new IllegalStateException(syntaxMessage(name), e)
    }
    if (map.containsKey(name)) {
      throw new IllegalArgumentException("Key $name is already defined in ${this.name}")
    }
    Object arg = ConfigurableProxy.resolveSingleArgument("$name in ${this.name}", args)
    V value = resolveValue(key, arg)
    map[key] = value
  }

  private String syntaxMessage(String name) {
    return (
        "Use different syntax to define key of type $keyClass "
            + "('$name' cannot be intertpreted as $keyClass.simpleName)"
    )
  }

}
