package com.stanfy.helium.handler.codegen.java.retrofit;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.java.BaseJavaGenerator;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.HttpHeader;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.Type;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import static com.squareup.javawriter.JavaWriter.stringLiteral;
import static com.stanfy.helium.utils.Names.canonicalName;
import static com.stanfy.helium.utils.Names.prettifiedName;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates a Retrofit interface for a web service.
 */
public class RetrofitInterfaceGenerator extends BaseJavaGenerator<RetrofitGeneratorOptions> implements Handler {

  /** Package name. */
  private static final String RETROFIT_PACKAGE = "retrofit.http.";

  public RetrofitInterfaceGenerator(final File outputDir, final RetrofitGeneratorOptions options) {
    super(outputDir, options);
  }

  @Override
  public void handle(final Project project) {
    File dest = getPackageDirectory();
    for (Service service : project.getServices()) {
      String name = getOptions().getName(service);
      File serviceFile = new File(dest, name.concat(EXT_JAVA));
      try {
        write(service, serviceFile, name);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

  }

  private String resolveJavaTypeName(final Type type, final JavaWriter writer) {
    Type imported = type;
    boolean sequence = false;
    if (imported instanceof Sequence) {
      sequence = true;
      imported = ((Sequence) imported).getItemsType();
    }
    String javaType = getOptions().getJavaTypeName(imported, sequence, writer);
    if (!type.isPrimitive() && getOptions().getEntitiesPackage() != null) {
      javaType = getOptions().getEntitiesPackage() + "." + javaType;
    }
    return writer.compressType(javaType);
  }

  private void addImport(final Set<String> imports, final Type type, final JavaWriter writer) {
    String name = resolveJavaTypeName(type, writer);
    if (type instanceof Sequence && getOptions().getSequenceCollectionName() != null) {
      imports.add(getOptions().getSequenceCollectionName());
    }
    imports.add(name);
  }

  private static String getTransformedPath(final ServiceMethod m) {
    return m.getPath().replaceAll("@(\\w+)", "{$1}");
  }

  private void write(final Service service, final File dest, final String serviceClassName) throws IOException {
    JavaWriter writer = new JavaWriter(new OutputStreamWriter(new FileOutputStream(dest), "UTF-8"));
    RetrofitGeneratorOptions options = getOptions();

    try {
      writer.emitPackage(options.getPackageName());

      HashSet<String> imports = new HashSet<String>();
      for (ServiceMethod m : service.getMethods()) {
        if (options.getEntitiesPackage() != null) {
          if (m.getResponse() != null) {
            addImport(imports, m.getResponse(), writer);
          }
          if (m.getBody() != null) {
            addImport(imports, m.getBody(), writer);
          }
        }
        if (m.getResponse() == null) {
          imports.add("retrofit.client.Response");
        }
      }

      writer.emitImports(imports);
      writer.emitImports(RETROFIT_PACKAGE.concat("*"));
      writer.emitEmptyLine();

      writer.beginType(serviceClassName, "interface", EnumSet.of(PUBLIC));
      writer.emitEmptyLine();

      if (service.getLocation() != null) {
        writer.emitField("String", "DEFAULT_URL", EnumSet.noneOf(Modifier.class), stringLiteral(service.getLocation()));
        writer.emitEmptyLine();
      }

      for (ServiceMethod m : service.getMethods()) {
        if (m.getBody() != null && m.getBody().isAnonymous()) {
          continue;
        }

        writeJavaDoc(writer, m);

        List<String> constantHeaders = new ArrayList<String>();
        for (HttpHeader header : m.getHttpHeaders()) {
          if (header.isConstant()) {
            constantHeaders.add(stringLiteral(header.getName() + ": " + header.getValue()));
          }
        }
        if (!constantHeaders.isEmpty()) {
          writer.emitAnnotation("Headers", constantHeaders.toArray(new String[constantHeaders.size()]));
        }

        writer.emitAnnotation(m.getType().toString(), stringLiteral(getTransformedPath(m)));

        String responseType = "Response";
        if (m.getResponse() != null) {
          responseType = resolveJavaTypeName(m.getResponse(), writer);
        }
        writer.beginMethod(responseType, options.getMethodName(m), EnumSet.noneOf(Modifier.class),
            resolveParameters(m, writer), Collections.<String>emptyList());
        writer.endMethod();
        writer.emitEmptyLine();
      }

      writer.endType();
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  private void writeJavaDoc(final JavaWriter writer, final ServiceMethod m) throws IOException {
    StringBuilder javadoc = new StringBuilder();
    if (m.getName() != null) {
      javadoc.append(m.getName());
      if (javadoc.length() > 0 && javadoc.charAt(javadoc.length() - 1) != '.') {
        javadoc.append('.');
      }
    }
    if (m.getDescription() != null) {
      if (javadoc.length() > 0) {
        javadoc.append("\n");
      }
      javadoc.append(m.getDescription());
    }
    if (javadoc.length() > 0) {
      writer.emitJavadoc(javadoc.toString());
    }
  }

  private String getJavaType(final Type type, final JavaWriter writer) {
    String name = resolveJavaTypeName(type, writer);
    if (type instanceof Sequence) {
      return getOptions().getSequenceTypeName(name);
    }
    return name;
  }

  private List<String> resolveParameters(final ServiceMethod m, final JavaWriter writer) {
    ArrayList<String> res = new ArrayList<String>();
    if (m.hasRequiredParametersInPath()) {
      for (String pp : m.getPathParameters()) {
        res.add("@Path(" + stringLiteral(pp) + ") String");
        res.add(getOptions().getSafeParameterName(pp));
      }
    }

    if (m.hasRequiredHeaders()) {
      for (HttpHeader header : m.getHttpHeaders()) {
        if (!header.isConstant()) {
          res.add("@Header(" + stringLiteral(header.getName()) + ") String");
          res.add("header".concat(prettifiedName(canonicalName(header.getName()))));
        }
      }
    }

    if (m.getParameters() != null) {
      for (Field f : m.getParameters().getActiveFields()) {
        res.add("@Query(\"" + f.getName() + "\") " + getJavaType(f.getType(), writer));
        res.add(getOptions().getSafeParameterName(f.getCanonicalName()));
      }
    }

    if (m.getBody() != null) {
      res.add("@Body " + getJavaType(m.getBody(), writer));
      res.add("body");
    }

    return res;
  }

}
