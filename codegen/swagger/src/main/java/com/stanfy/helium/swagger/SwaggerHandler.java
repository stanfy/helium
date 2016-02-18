package com.stanfy.helium.swagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.stanfy.helium.DefaultType;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.json.schema.JsonType;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Project;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Consumes a Helium project and produces a Swagger spec. */
public class SwaggerHandler implements Handler {

  private static final Map<String, JsonType> TYPES_MAPPING = new HashMap<>();
  static {
    TYPES_MAPPING.put(DefaultType.INT32.getLangName(), JsonType.INTEGER);
    TYPES_MAPPING.put(DefaultType.INT64.getLangName(), JsonType.INTEGER);
    TYPES_MAPPING.put(DefaultType.BOOL.getLangName(), JsonType.BOOLEAN);
    TYPES_MAPPING.put(DefaultType.DOUBLE.getLangName(), JsonType.NUMBER);
    TYPES_MAPPING.put(DefaultType.FLOAT.getLangName(), JsonType.NUMBER);
    TYPES_MAPPING.put(DefaultType.BYTES.getLangName(), JsonType.STRING);
    TYPES_MAPPING.put(DefaultType.STRING.getLangName(), JsonType.STRING);
  }

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .create();

  private final File destination;

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
      LinkedHashMap<String, Definition> definitions = new LinkedHashMap<>();
      if (!service.getMethods().isEmpty()) {
        LinkedHashMap<String, Path> paths = new LinkedHashMap<>(service.getMethods().size());
        for (ServiceMethod m : service.getMethods()) {
          Path.Method method = swaggerPath(paths, m).swaggerMethod(m);
          method.summary = m.getName();
          method.description = m.getDescription();

          if (m.getParameters() != null) {
            ArrayList<Parameter> params = new ArrayList<>(m.getParameters().getFields().size());
            for (Field f : m.getParameters().getFields()) {
              Parameter parameter = new Parameter();
              parameter.name = f.getName();
              parameter.description = f.getDescription();
              parameter.in = "query";
              parameter.type = convertType(f.getType());
              parameter.required = f.isRequired();
              if (JsonType.NUMBER.getName().equals(parameter.type)) {
                parameter.format = f.getType().getName();
              }

              params.add(parameter);
            }
            method.parameters = params;
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

  private Path swaggerPath(Map<String, Path> map, ServiceMethod m) {
    Path p = map.get(m.getPath());
    if (p == null) {
      p = new Path();
      map.put(m.getPath(), p);
    }
    return p;
  }

  private String convertType(Type type) {
    if (!type.isPrimitive()) {
      throw new IllegalArgumentException(type + " is not primitive");
    }
    JsonType json = TYPES_MAPPING.get(type.getName());
    if (json == null) {
      throw new UnsupportedOperationException("Unknown type " + type);
    }
    return json.getName();
  }
}
