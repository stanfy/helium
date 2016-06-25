package com.stanfy.helium.handler.codegen.objectivec.entity.classtree;

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePartsContainer
import java.util.*

/**
 * Created by paultaykalo on 12/16/15.
 */
open class ObjCMethodSourcePart(val method: ObjCMethod) : ObjCSourcePartsContainer() {

  fun asString(includeBody: Boolean): String {
    val bld = StringBuilder();
    bld.append(when (method.methodType) {
      ObjCMethod.ObjCMethodType.CLASS -> "+"
      ObjCMethod.ObjCMethodType.INSTANCE -> "-"
    });
    bld.append("(").append(method.returnType).append(")");
    bld.append(method.name);
    for (parameter in  method.parameters) {
      if (parameter == method.parameters.first()) {
        bld.append("With")
        bld.append(parameter.name.capitalize())
      } else {
        bld.append(parameter.name)
      }
      bld.append(":(");
      bld.append(parameter.type);
      bld.append(")");
      bld.append(parameter.name);
      bld.append(" ");
    }
    if (includeBody) {
      bld.append(" {\n");
      bld.append(super.asString());
      bld.append("\n}");
    } else {
      bld.append(";")
    }
    return bld.toString();
  }
}

class ObjCMethodImplementationSourcePart(method: ObjCMethod) : ObjCMethodSourcePart(method) {
  override fun asString(): String {
    return asString(includeBody = true)
  }
}

class ObjCMethodDeclarationSourcePart(method: ObjCMethod) : ObjCMethodSourcePart(method) {
  override fun asString(): String {
    return asString(includeBody = false)
  }
}
