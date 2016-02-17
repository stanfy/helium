package com.stanfy.helium.internal.dsl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.format.ConvertValueSyntaxException
import com.stanfy.helium.format.json.ClosureJsonConverter
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.Constraint
import com.stanfy.helium.internal.utils.ConfigurableProxy
import com.stanfy.helium.internal.utils.DslUtils
import groovy.transform.PackageScope
import org.joda.time.format.DateTimeFormat

/**
 * Proxy for type.
 */
class ConfigurableType extends ConfigurableProxy<Type> {

  @PackageScope Map<String, Closure<?>> readers = [:], writers = [:];

  private String baseTypeName
  private ArrayList<Constraint<?>> constraints

  ConfigurableType(final Type core, final ProjectDsl project) {
    super(core, project)
  }

  void from(final String format, final Closure<?> spec) {
    readers[format] = DslUtils.runWithProxy(new From(), spec) as Closure<?>
  }

  void to(final String format, final Closure<?> spec) {
    writers[format] = DslUtils.runWithProxy(new To(), spec) as Closure<?>
  }

  void constraints(final String baseTypeName, final Closure<?> spec) {
    this.@baseTypeName = baseTypeName
    this.@constraints = new ArrayList<>()
    DslUtils.runWithProxy(new ConstraintsDsl(this.@constraints), spec)
  }

  @PackageScope String getBaseTypeName() {
    return this.@baseTypeName
  }

  @PackageScope ArrayList<Constraint<?>> getConstraints() {
    return this.@constraints
  }

  public class From {
    public Closure<?> asString() {
      return ClosureJsonConverter.AS_STRING_READER
    }
    public Closure<?> asDate(final String dateFormat) {
      return stringParser("Expected format: $dateFormat") { String str ->
        return DateTimeFormat.forPattern(dateFormat)
            .withLocale(Locale.US)
            .parseDateTime(str)
            .toDate()
      }
    }
    public Closure<?> parseString(Closure<?> parser) {
      return stringParser(null, parser)
    }

    private Closure<?> stringParser(String message, Closure<?> parser) {
      return { JsonReader input ->
        String str = (String) ClosureJsonConverter.AS_STRING_READER(input)
        if (str == null) { return null }
        try {
          return parser(str)
        } catch (IllegalArgumentException e) {
          throw new ConvertValueSyntaxException(str, message ? message : e.message)
        }
      }
    }
  }

  public class To {
    public Closure<?> asString() {
      return ClosureJsonConverter.AS_STRING_WRITER
    }
    public Closure<?> asDate(final String dateFormat) {
      return { JsonWriter input, Object value ->
        if (value == null) {
          input.nullValue()
          return
        }
        if (value instanceof Date) {
          input.value(DateTimeFormat.forPattern(dateFormat)
              .withLocale(Locale.US)
              .print(value.time))
          return
        }
        if (value instanceof String) {
          DateTimeFormat.forPattern(dateFormat).parseDateTime(value.toString()) // try to parse
          input.value((String)value)
          return
        }

        throw new IllegalArgumentException("Cannot interpret '$value' as date. Format: '$dateFormat'.")
      }
    }
    public Closure<?> formatToString(final Closure<String> formatter) {
      return { JsonWriter input, Object value ->
        if (value == null) {
          input.nullValue()
          return
        }
        input.value(formatter(value))
      }
    }

  }

}
