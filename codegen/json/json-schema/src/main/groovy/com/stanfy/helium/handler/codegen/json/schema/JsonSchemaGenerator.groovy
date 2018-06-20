package com.stanfy.helium.handler.codegen.json.schema

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic
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

  private final SchemaBuilder builder = new SchemaBuilder()

  public JsonSchemaGenerator(File outputDirectory, JsonSchemaGeneratorOptions options) {
    super(outputDirectory, options);
    this.schemeOutputDir = getOutputDirectory();

    gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(JsonType.class, new JsonTypeAdapter().nullSafe())
        .create()
  }

  @Override
  void handle(Project project) {
    JsonSchemaGeneratorOptions options = getOptions()

    for (Type type : project.getTypes().all()) {
      boolean shouldProcess = options.isTypeUserDefinedMessage(type) && options.isTypeIncluded(type);
      if (shouldProcess) {
        final File schemaFile = new File(schemeOutputDir, type.getCanonicalName().concat(JSON_SCHEME_SUFFIX)
            .concat(EXT_JSON))
        FileWriter fileWriter = null
        try {
          fileWriter = new FileWriter(schemaFile)
          write((Message) type, fileWriter)
        } catch (IOException e) {
          throw new RuntimeException(e)
        } finally {
          IOUtils.closeQuietly(fileWriter)
        }
      }
    }
  }

  void write(Message msg, Appendable writer) {
    def schema = builder.makeSchemaFromMessage(msg, SchemaBuilder.FULL_SELECTION)
    schema.schema = JSON_SCHEMA_DRAFT_4_URI

    gson.toJson(schema, writer)
  }

  @CompileStatic
  static class JsonTypeAdapter extends TypeAdapter<JsonType> {
    @Override
    void write(JsonWriter out, JsonType value) throws IOException {
      out.value(value.getName())
    }

    @Override
    JsonType read(JsonReader jsonReader) throws IOException {
      return JsonType.valueOf(jsonReader.nextString().toUpperCase(Locale.US))
    }
  }

}
