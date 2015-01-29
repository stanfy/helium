package com.stanfy.helium.handler.codegen.java.retrofit;

import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.utils.Names;

/**
 * Options for Retorfit generator.
 */
public class RetrofitGeneratorOptions extends JavaGeneratorOptions {

  /** Entities package. */
  private String entitiesPackage;

  /** Whether to use service method names to generate java method names. */
  private boolean useMethodNames;

  /** Used when generating retrofit interfaces */
  boolean useCallback;

  public static RetrofitGeneratorOptions defaultOptions(final String packageName) {
    RetrofitGeneratorOptions options = new RetrofitGeneratorOptions();
    options.setPackageName(packageName);
    options.setPrettifyNames(true);
    options.setUseMethodNames(true);
    return options;
  }

  public void setEntitiesPackage(final String entitiesPackage) {
    this.entitiesPackage = entitiesPackage;
  }

  public String getEntitiesPackage() {
    return entitiesPackage;
  }

  public boolean isUseMethodNames() {
    return useMethodNames;
  }

  public void setUseMethodNames(final boolean useMethodNames) {
    this.useMethodNames = useMethodNames;
  }

  String getMethodName(final ServiceMethod method) {
    if (!useMethodNames || method.getName() == null) {
      return getName(method);
    }
    String name = Names.canonicalName(method.getName());
    if (isPrettifyNames()) {
      String res = Names.prettifiedName(name);
      name = String.valueOf(Character.toLowerCase(res.charAt(0)));
      if (res.length() > 1) {
        name = name.concat(res.substring(1));
      }
    }
    return name;
  }

  boolean useRetrofitCallback() {
    return this.useCallback;
  }

}
