package com.stanfy.helium.handler.codegen.objectivec.entity.httpclient.afnetworking

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCPropertyNameTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCTypeTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.*
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod

/**
 * Created by paultaykalo on 12/18/15.
 *
 */
class ObjCAFNetworkingHTTPClientGenerator:ObjCProjectStructureGenerator {

  // TODO : Ibject theser values to the generator
  public val nameTransformer = ObjCPropertyNameTransformer()
  public val typeTransformer = ObjCTypeTransformer()

  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {
    projectDSL.services.forEach { service ->
      val mappingsClassName = options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "Client"
      val mappingsClass = ObjCClass(mappingsClassName)
      project.classStructure.addClass(mappingsClass)

      addInitMethodForService(mappingsClass, service)
      service.methods.forEach { method ->
        addServiceMethodForService(project,mappingsClass, service, method)
      }
    }

  }

  private fun addInitMethodForService(mappingsClass: ObjCClass, service: Service) {
    // Version and location setup
    mappingsClass.forwardDeclarations.add("AFHTTPClient")
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("version", ObjCType("NSString", isReference = true)))
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("name", ObjCType("NSString", isReference = true)))
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("baseURL", ObjCType("NSURL", isReference = true)))
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("httpClient", ObjCType("AFHTTPClient", isReference = true)))

    val initMethod = ObjCMethod("init", ObjCMethod.ObjCMethodType.INSTANCE, "id")
    val initMethodImplementationSourcePart = ObjCMethodImplementationSourcePart(initMethod)
    initMethodImplementationSourcePart.addSourcePart(ObjCStringSourcePart(
        """
  self = [super init];
  if (self) {
    self.version = "${service.version}";
    self.name = "${service.name}";
    self.baseURL = [NSURL URLWithString:@"${service.location}"];
    self.client = [AFHTTPClient clientWithBaseURL:self.baseURL];
  }
  return self
          """
    ));
    mappingsClass.implementation.addBodySourcePart(initMethodImplementationSourcePart)
    mappingsClass.implementation.importClassWithName("AFHTTPClient")
  }

  private fun addServiceMethodForService(project:ObjCProject, mappingsClass: ObjCClass, service: Service, method: ServiceMethod) {
    val methodname = Names.prettifiedName(Names.canonicalName(method.name)).decapitalize()
    val serviceMethod = ObjCMethod(methodname, ObjCMethod.ObjCMethodType.INSTANCE, "void")
    if (method.pathParameters != null) {
      method.pathParameters.forEach { paramName ->
        serviceMethod.addParameter("NSString *", Names.prettifiedName(Names.canonicalName(paramName)))
      }
    }
    if (method.parameters != null) {
      method.parameters.fields.forEach { field ->
        serviceMethod.addParameter(typeTransformer.objCType(field.type).toString(), Names.prettifiedName(Names.canonicalName(field.name)))
      }
    }
    mappingsClass.implementation.addMethod(serviceMethod)
    mappingsClass.definition.addMethod(serviceMethod)
  }

}