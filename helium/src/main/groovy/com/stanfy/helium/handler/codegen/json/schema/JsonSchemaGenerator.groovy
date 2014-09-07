package com.stanfy.helium.handler.codegen.json.schema

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.stanfy.helium.DefaultType
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.constraints.EnumConstraint
import org.apache.commons.io.IOUtils

/**
 * Type JSON-scheme (http://json-schema.org/) generator.
 *
 * @author Michael Pustovit mpustovit@stanfy.com.ua
 */
class JsonSchemaGenerator extends BaseGenerator<JsonSchemaGeneratorOptions> implements Handler {
  /** JSON file extension. */
  public static final String EXT_JSON = ".json";

  /** JSON scheme file suffix (e.g. HelloWorldScheme.json) */
  public static final String JSON_SCHEME_SUFFIX = "Schema";

  public static final String JSON_SCHEMA_DRAFT_4_URI = "http://json-schema.org/draft-04/schema#"

  private final File schemeOutputDir;

  private final Gson gson;

  public JsonSchemaGenerator(File outputDirectory, JsonSchemaGeneratorOptions options) {
    super(outputDirectory, options);
    this.schemeOutputDir = getOutputDirectory();

    gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(JsonType.class, new JsonTypeAdapter())
        .create()
  }

  @Override
  public void handle(Project project) {
    JsonSchemaGeneratorOptions options = getOptions();

    for (Type type : project.getTypes().all()) {
      boolean shouldProcess = options.isTypeUserDefinedMessage(type) && options.isTypeIncluded(type);
      if (shouldProcess) {
        final File schemaFile = new File(schemeOutputDir, type.getCanonicalName().concat(JSON_SCHEME_SUFFIX)
            .concat(EXT_JSON));
        FileWriter fileWriter = null;
        try {
          fileWriter = new FileWriter(schemaFile);
          write((Message) type, fileWriter);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } finally {
          IOUtils.closeQuietly(fileWriter);
        }
      }
    }
  }

  JsonSchemaEntity makeSchemaFromMessage(Message msg) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.OBJECT
    schema.description = msg.getDescription()

    if (msg.fields) {
      msg.activeFields.each { field ->
        def property = makeSchemaFromType(field.getType())
        property.description = field.getDescription()
        schema.addProperty(field.name, property)
      }

      msg.fields.grep({it.required }).each({schema.addRequired(it.name)})
    }

    return schema
  }

  JsonSchemaEntity makeSchemaFromSequence(Sequence sequence) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.ARRAY
    schema.description = sequence.getDescription()
    schema.items = makeSchemaFromType(sequence.itemsType)

    return schema
  }

  JsonSchemaEntity makeSchemaFromType(Type type) {
    def property

    def jsonType = translateType(type)

    switch (jsonType) {
      case JsonType.OBJECT:
        property = makeSchemaFromMessage((Message) type)
        break
      case JsonType.ARRAY:
        property = makeSchemaFromSequence((Sequence) type)
        break
      case JsonType.ENUM:
        property = new JsonSchemaEntity()
        EnumConstraint<String> constraint = ((ConstrainedType) type)
            .getConstraint(EnumConstraint.class) as EnumConstraint<String>
        property.enumeration = new ArrayList<>(constraint.values)
        break
      default:
        property = new JsonSchemaEntity()
        property.type = jsonType
    }

    return property
  }

  void write(Message msg, Appendable writer) {
    def schema = makeSchemaFromMessage(msg)
    schema.schema = JSON_SCHEMA_DRAFT_4_URI

    gson.toJson(schema, writer)
  }

  static JsonType translateType(Type type) {
    if (type instanceof ConstrainedType) {
      if (type.containsConstraint(EnumConstraint)) {
        return JsonType.ENUM
      }
      return translateType(type.baseType)
    }
    if (type.isPrimitive()) {
      switch (type.name) {
        case DefaultType.DOUBLE.langName:
        case DefaultType.FLOAT.langName:
          return JsonType.NUMBER
        case DefaultType.INT32.langName:
        case DefaultType.INT64.langName:
          return JsonType.INTEGER
        case DefaultType.BOOL.langName:
          return JsonType.BOOLEAN
        case DefaultType.STRING.langName:
        case DefaultType.BYTES.langName:
        default:
          return JsonType.STRING
      }
    } else {
      if (type instanceof Message) {
        return JsonType.OBJECT
      }

      if (type instanceof Sequence) {
        return JsonType.ARRAY
      }
    }

    throw new IllegalArgumentException("'${type.name}' simple type isn't supported")
  }

  static class JsonTypeAdapter implements JsonSerializer<JsonType> {
    @Override
    JsonElement serialize(JsonType src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getName())
    }
  }

}
