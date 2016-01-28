package com.stanfy.helium.handler.codegen.objectivec.entity.httpclient.urlsession

import com.github.mustachejava.DefaultMustacheFactory
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCPropertyNameTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCTypeTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.*
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AccessModifier
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import java.io.StringWriter

/**
 * Created by paultaykalo on 12/18/15.
 *
 */
class ObjCHTTPClientGenerator(val typeTransformer: ObjCTypeTransformer,
                              val nameTransformer: ObjCPropertyNameTransformer) : ObjCProjectStructureGenerator  {

  private var generationOptions:ObjCEntitiesOptions? = null;

  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {

    this.generationOptions = options
    projectDSL.services.forEach { service ->
      addPregeneratedClientWithName(project, service)
      val httpClientClass = ObjCClass(this.httpClientClassNameForService(service, this.generationOptions!!))
      project.classesTree.addClass(httpClientClass)

      val apiClassName = options.prefix + Names.prettifiedName(Names.canonicalName(service.name))
      val apiClass = ObjCClass(apiClassName)
      project.classesTree.addClass(apiClass)

      addInitMethodForService(service,apiClass)
      addCancellableDependencyForService(service, apiClass)
      service.methods.forEach { method ->
        addServiceMethodForService(service, apiClass, method, project)
      }
      addDeserializationLogicForService(service, apiClass);
    }
  }

  private fun addCancellableDependencyForService(service:Service, apiClass: ObjCClass) {
    val cancellableOperationClassName = this.cancelableOperationClassNameForService(service,this.generationOptions!!)
    apiClass.addProtocolForwardDeclaration(cancellableOperationClassName)
    apiClass.implementation.importClassWithName(cancellableOperationClassName)
  }

  private fun addInitMethodForService(service: Service, apiClass: ObjCClass) {
    val httpClientClassName = this.httpClientClassNameForService(service, this.generationOptions!!)
    // Version and location setup
    apiClass.addClassForwardDeclaration(httpClientClassName)
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("version", ObjCType("NSString")))
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("name", ObjCType("NSString")))
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("httpClient", ObjCType(httpClientClassName)))

    apiClass.definition.addComplexPropertySourcePart("""
    /**
     * Block that called to transform response data to the provided destination class
     */
    @property(nonatomic, copy) id (^responseDeserializerBlock)(NSHTTPURLResponse * response, NSData * responseData, Class destinationClass, NSURLRequest * request, NSError ** error);
    /**
     * bloch that is being called when object request body need to be transformed to NSData
     */
    @property(nonatomic, copy) NSData *(^requestBodySerializerBlock)(id bodyToSerialize, NSError ** error);
    """)


    val initMethod = ObjCMethod("init", ObjCMethod.ObjCMethodType.INSTANCE, "id")
    val initMethodImplementationSourcePart = ObjCMethodImplementationSourcePart(initMethod)
    initMethodImplementationSourcePart.addSourcePart(ObjCStringSourcePart(
        """
        self = [super init];
        if (self) {
            self.httpClient = [$httpClientClassName new];
            self.version = [self.httpClient version];
            self.name = @"${service.name}";
        }
        return self;
        """
    ));
    apiClass.implementation.addBodySourcePart(initMethodImplementationSourcePart)
    apiClass.implementation.importClassWithName(httpClientClassName)
  }

  private fun addServiceMethodForService(service: Service, apiClass: ObjCClass, method: ServiceMethod, project: ObjCProject) {
    val cancellableOperationClassName = this.cancelableOperationClassNameForService(service,this.generationOptions!!)
    val methodName = Names.prettifiedName(Names.canonicalName(method.name)).decapitalize()
    val serviceMethod = ObjCMethod(methodName, ObjCMethod.ObjCMethodType.INSTANCE, "id<$cancellableOperationClassName>")


    val paramsBuilder = StringBuilder("NSMutableDictionary * _params = [NSMutableDictionary dictionary];\n")
    val pathBuilder = StringBuilder("NSString * _path = [NSString stringWithFormat:@\"${method.path}\"];\n")

    // Injecting Path Parameters to method
    if (method.pathParameters != null) {
      method.pathParameters.forEach { paramName ->
        val parameterName = Names.prettifiedName(Names.canonicalName(paramName))
        val checkedParameterName = nameTransformer.propertyNameFrom(parameterName)
        serviceMethod.addParameter("NSString *", checkedParameterName)
        pathBuilder.append(
         "     _path = [_path stringByReplacingOccurrencesOfString:@\"@$paramName\" withString:$checkedParameterName];\n"
        )
      }
    }
    // Injecting Method Parameters to method
    if (method.parameters != null) {
      method.parameters.fields.forEach { field ->
        val parameterName = Names.prettifiedName(Names.canonicalName(field.name))
        val checkedParameterName = nameTransformer.propertyNameFrom(parameterName)
        val objCType = typeTransformer.objCType(field.type)
        serviceMethod.addParameter(objCType.toString(), checkedParameterName)

        // Wrapping simple values into the NSNumber
        if (objCType.isReference) {
          paramsBuilder.append("""
            if ($checkedParameterName) {
               _params[@"${field.name}"] = $checkedParameterName;
            }
          """)
        } else {
          paramsBuilder.append("""
           _params[@"${field.name}"] = @($checkedParameterName);
          """)
        }
      }
    }

    // check body parameter for put methods
    var body = "nil"
    val bodyBuilder = StringBuilder()
    if (method.hasBody()) {
      val objcClass = project.classesTree.getClassForType(method.body.name)
      var bodyType:String
      if (objcClass == null) {
        bodyType = typeTransformer.objCType(method.body).toString()
      } else {
        bodyType = "${objcClass.name} *"
      }
      serviceMethod.addParameter(bodyType, "body")
      body = "serializedBody"
      bodyBuilder.append("""
        NSData * $body = nil;
        NSError * serializeRequestBodyError = nil;
        if (self.requestBodySerializerBlock) {
            $body = self.requestBodySerializerBlock(body,&serializeRequestBodyError);
        }
        if (!$body) {
           if (failure) {
              failure(serializeRequestBodyError);
           }
           return nil;
        }
      """)
    }

    var responseType = "id"
    var responseClassType = "NSObject"
    if (method.response is Message) {
      val objcClass = project.classesTree.getClassForType(method.response.name)!!
      responseClassType = objcClass.name
      responseType = "${objcClass.name} *"
      apiClass.classesForwardDeclarations.add(objcClass.name)
    }

    // Adding success and failure methods
    serviceMethod.addParameter("void (^)(${responseType})", "success")
    serviceMethod.addParameter("void (^)(NSError *)", "failure")

    // HTTP Method
    val httpMethod = method.type.name.toUpperCase()

    // Method implementation
    val serviceMethodImplementation = ObjCMethodImplementationSourcePart(serviceMethod)
    serviceMethodImplementation.addSourcePart(ObjCStringSourcePart(
        """
    ${pathBuilder.toString()}
    ${paramsBuilder.toString()}
    ${bodyBuilder.toString()}
    return [self.httpClient
        sendRequestWithDescription:@"${method.name}"
                              path:_path
                        parameters:_params
                              body:$body
                           headers:nil
                            method:@"$httpMethod"
                      successBlock:[self deserializationBlockForClass:[$responseClassType class] successBlock:success failureBlock:failure]
                      failureBlock:^(NSError *error) {
                         if (failure) { failure(error); }
        }];
        """
    ));
    apiClass.implementation.addBodySourcePart(serviceMethodImplementation)
    apiClass.definition.addMethod(serviceMethod)
    apiClass.implementation.importClassWithName(responseClassType)
  }

  private fun addPregeneratedClientWithName(project: ObjCProject, service: Service) {
    val httpClientClassName = httpClientClassNameForService(service, this.generationOptions!!)
    val cancellableOperationClassName = cancelableOperationClassNameForService(service, this.generationOptions!!)

    val templateObject: Any = object : Any() {
      val cancelableOperationClassName:String  = cancellableOperationClassName
      val httpClientClassName:String = httpClientClassName
      val service:Service = service
    }

    project.classesTree.pregeneratedClasses.add(
        ObjCPregeneratedClass(httpClientClassName,
            generatedTemplateWithName("HTTPClientHeader.mustache", templateObject),
            generatedTemplateWithName("HTTPClientImplementation.mustache", templateObject)))

    project.classesTree.pregeneratedClasses.add(
        ObjCPregeneratedClass(cancellableOperationClassName,
            generatedTemplateWithName("CancelableOperationHeader.mustache", templateObject),
            null))
  }

  private fun generatedTemplateWithName(templateName:String, templateObject:Any):String {
    val mustacheFactory = DefaultMustacheFactory()
    val mustache = mustacheFactory.compile(templateName)
    val stringWriter = StringWriter()
    mustache.execute(stringWriter, templateObject)
    return stringWriter.toString()
  }
  private fun cancelableOperationClassNameForService(service:Service, options: ObjCEntitiesOptions):String {
    return options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "CancelableOperation"
  }

  private fun httpClientClassNameForService(service:Service, options: ObjCEntitiesOptions):String {
    return options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "Client"
  }

  private fun addDeserializationLogicForService(service:Service, apiClass: ObjCClass) {

    apiClass.implementation.addBodySourcePart(ObjCStringSourcePart(
        """
        - (void (^)(NSHTTPURLResponse *, NSData *, NSURLRequest *))deserializationBlockForClass:(Class)clz successBlock:(void (^)(id))successBlock failureBlock:(void (^)(NSError *))failureBlock {
    return ^(NSHTTPURLResponse *response, NSData *rawData, NSURLRequest *request) {
        id result = rawData;
        if (self.responseDeserializerBlock) {
            NSError *serializationError = nil;
            result = self.responseDeserializerBlock(response, rawData, clz, request, &serializationError);
            if (!result) {
                if (serializationError) {
                    if (failureBlock) {
                        failureBlock(serializationError);
                    }
                    return;
                }
            }
        }

        if (successBlock) { successBlock(result); }
    };
}

        """
    ))
  }


}