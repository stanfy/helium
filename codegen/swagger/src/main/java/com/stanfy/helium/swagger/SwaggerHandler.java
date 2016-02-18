package com.stanfy.helium.swagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaEntity;
import com.stanfy.helium.handler.codegen.json.schema.JsonSchemaGenerator;
import com.stanfy.helium.handler.codegen.json.schema.JsonType;
import com.stanfy.helium.handler.codegen.json.schema.SchemaBuilder;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.Type;
import okio.Okio;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consumes a Helium project and produces a Swagger spec. */
public class SwaggerHandler implements Handler {

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(JsonType.class, new JsonSchemaGenerator.JsonTypeAdapter().nullSafe())
      .create();

  private final File destination;

  private final SchemaBuilder schemaBuilder = new SchemaBuilder();

  public SwaggerHandler(File destination) {
    if (destination.exists() && !destination.isDirectory()) {
      throw new IllegalArgumentException("Destination must be a folder");
    }
    this.destination = destination;
  }

  @Override
  public void handle(Project project) {
    if (!destination.exists()) {
      if (!destination.mkdirs()) {
        throw new RuntimeException("Cannot create destination dir " + destination);
      }
    }
    for (Service service : project.getServices()) {
      Root spec = buildSwagger(service);
      File specFile = new File(destination, service.getCanonicalName().concat(".json"));
      Writer output = null;
      try {
        output = new OutputStreamWriter(Okio.buffer(Okio.sink(specFile)).outputStream(), "UTF-8");
        GSON.toJson(spec, output);
      } catch (JsonIOException | IOException e) {
        throw new RuntimeException("Cannot write spec for service " + service.getName(), e);
      } finally {
        IOUtils.closeQuietly(output);
      }
    }
  }

  private Root buildSwagger(Service service) {
    if (service.getName() == null) {
      throw new IllegalStateException("Service must have a name");
    }
    Root root = new Root();
    root.info = new Root.Info(service.getName(), service.getDescription(), service.getVersion());
    if (service.getLocation() != null) {
      // Basic data.
      try {
        URI uri = URI.create(service.getLocation());
        root.schemes = Collections.singletonList(uri.getScheme());
        root.host = uri.getHost();
        if (uri.getPort() != -1) {
          root.host += ":" + uri.getPort();
        }
        root.basePath = uri.getRawPath();
      } catch (IllegalArgumentException e) {
        root.basePath = service.getLocation();
      }

      // Paths and definitions.
      LinkedHashMap<String, JsonSchemaEntity> definitions = new LinkedHashMap<>();
      if (!service.getMethods().isEmpty()) {
        LinkedHashMap<String, Path> paths = new LinkedHashMap<>(service.getMethods().size());
        for (ServiceMethod m : service.getMethods()) {
          Path.Method method = swaggerPath(paths, m).swaggerMethod(m);
          method.summary = m.getName();
          method.description = m.getDescription();
          queryParameters(m, method);

          if (m.getResponse() != null) {
            ensureDefinition(m.getResponse(), definitions);
            String link = "#/definitions/".concat(m.getResponse().getName());
            method.responses = Collections.singletonMap("200", new Path.Response(link));
          }
        }
        root.paths = paths;
      }
      if (!definitions.isEmpty()) {
        root.definitions = definitions;
      }
    }
    return root;
  }

  private void queryParameters(ServiceMethod m, Path.Method method) {
    if (m.getParameters() != null) {
      ArrayList<Parameter> params = new ArrayList<>(m.getParameters().getFields().size());
      for (Field f : m.getParameters().getFields()) {
        Parameter parameter = new Parameter();
        parameter.name = f.getName();
        parameter.description = f.getDescription();
        parameter.in = "query";
        parameter.type = schemaBuilder.translateType(f.getType()).getName();
        parameter.required = f.isRequired();
        // TODO: Handle formats properly.
        if (JsonType.NUMBER.getName().equals(parameter.type)) {
          parameter.format = f.getType().getName();
        }

        params.add(parameter);
      }
      method.parameters = params;
    }
  }

  private void ensureDefinition(Type type, Map<String, JsonSchemaEntity> definitions) {
    if (type.isPrimitive()) {
      return;
    }

    JsonSchemaEntity entity = definitions.get(type.getName());
    if (entity != null) {
      return;
    }

    definitions.put(type.getName(), schemaBuilder.makeSchemaFromType(type));
    List<Type> nextTypes = Collections.emptyList();
    if (type instanceof Sequence) {
      nextTypes = Collections.singletonList(((Sequence) type).getItemsType());
    } else if (type instanceof Dictionary) {
      Dictionary dict = (Dictionary) type;
      nextTypes = Arrays.asList(dict.getKey(), dict.getValue());
    }
    for (Type t : nextTypes) {
      if (!t.isAnonymous()) {
        ensureDefinition(t, definitions);
      }
    }
  }

  private static Path swaggerPath(Map<String, Path> map, ServiceMethod m) {
    Path p = map.get(m.getPath());
    if (p == null) {
      p = new Path();
      map.put(m.getPath(), p);
    }
    return p;
  }

}
