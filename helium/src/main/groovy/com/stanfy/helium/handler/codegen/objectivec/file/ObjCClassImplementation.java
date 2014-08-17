package com.stanfy.helium.handler.codegen.objectivec.file;

/**
 * Created by ptaykalo on 8/17/14.
 * Simple block that know how to serialize ObjC Class Implementation
 */
public class ObjCClassImplementation implements ObjCSourcePart {

  /*
  Class Name
   */
  private String className;

  public ObjCClassImplementation(final String className) {
    this.className = className;
  }


  public String getClassName() {
    return className;
  }

  @Override
  public String asString() {
    // TODO use some templates
    return
        String.join("\n",
            "#import \""+className+".h\"",
            "@implementation " + className,
            "@end"
        );
  }
}
