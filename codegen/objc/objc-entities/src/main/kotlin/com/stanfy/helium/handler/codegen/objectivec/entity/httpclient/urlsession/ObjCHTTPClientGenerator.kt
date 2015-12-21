package com.stanfy.helium.handler.codegen.objectivec.entity.httpclient.urlsession

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCPropertyNameTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCTypeTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.*
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod

/**
 * Created by paultaykalo on 12/18/15.
 *
 */
class ObjCHTTPClientGenerator : ObjCProjectStructureGenerator {

  // TODO : Ibject theser values to the generator
  public val nameTransformer = ObjCPropertyNameTransformer()
  public val typeTransformer = ObjCTypeTransformer()

  private var generationOptions:ObjCEntitiesOptions? = null;

  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {
    this.generationOptions = options
    projectDSL.services.forEach { service ->
      addPregeneratedClientWithName(project, service)
      val httpClientClass = ObjCClass(this.httpClientClassNameForService(service, this.generationOptions!!))
      project.classStructure.addClass(httpClientClass)

      val apiClassName = options.prefix + Names.prettifiedName(Names.canonicalName(service.name))
      val apiClass = ObjCClass(apiClassName)
      project.classStructure.addClass(apiClass)

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
    val responseSerializerClassName = this.responsSerializerClassNameForService(service, this.generationOptions!!)
    // Version and location setup
    apiClass.addClassForwardDeclaration(httpClientClassName)
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("version", ObjCType("NSString", isReference = true)))
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("name", ObjCType("NSString", isReference = true)))
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("httpClient", ObjCType(httpClientClassName, isReference = true)))
    apiClass.definition.addPropertyDefinition(ObjCPropertyDefinition("responseDeserializer", ObjCType("NSObject<$responseSerializerClassName>", isReference = true)))
    apiClass.protocolsForwardDeclarations.add(responseSerializerClassName)

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

    var responseType = "id"
    var responseClassType = "NSObject"
    if (method.response is Message) {
      val objcClass = project.classStructure.getClassForType(method.response.name)!!
      responseClassType = objcClass.name
      responseType = "${objcClass.name} *"
      apiClass.classesForwardDeclarations.add(objcClass.name)
    }

    // Adding success and failure methods
    serviceMethod.addParameter("void (^)(${responseType})", "success")
    serviceMethod.addParameter("void (^)(NSError *)", "failure")

    // HTPP Method
    val httpMethod = method.type.name.toUpperCase()

    // Method implementation
    val serviceMethodImplementation = ObjCMethodImplementationSourcePart(serviceMethod)
    serviceMethodImplementation.addSourcePart(ObjCStringSourcePart(
        """
    ${pathBuilder.toString()}
    ${paramsBuilder.toString()}
    return [self.httpClient
        sendRequestWithDescription:@"${method.name}"
                              path:_path
                        parameters:_params
                              body:nil
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
    val httpClientClassName = this.httpClientClassNameForService(service, this.generationOptions!!)
    val cancellableOperationClassName = this.cancelableOperationClassNameForService(service,this.generationOptions!!)
    val implementation = httpClientImplementationForService(service)
    val header = httpClientHeaderForService(service)
    project.classStructure.pregeneratedClasses.add(ObjCPregeneratedClass(httpClientClassName, header, implementation))

    val cancellableOperationHeader = cancelableOperationHeaderForService(service)
    project.classStructure.pregeneratedClasses.add(ObjCPregeneratedClass(cancellableOperationClassName, cancellableOperationHeader, null))
  }

  private fun cancelableOperationClassNameForService(service:Service, options: ObjCEntitiesOptions):String {
    return options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "CancelableOperation"
  }

  private fun httpClientClassNameForService(service:Service, options: ObjCEntitiesOptions):String {
    return options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "Client"
  }
  private fun requestInterceptorClassNameForService(service: Service, options: ObjCEntitiesOptions): String {
    return options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "RequestInterceptor"
  }
  private fun responsSerializerClassNameForService(service: Service, options: ObjCEntitiesOptions): String {
    return options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "ResponseSerializer"
  }



  private fun addDeserializationLogicForService(service:Service, apiClass: ObjCClass) {
    val responseSerializerClassName = this.responsSerializerClassNameForService(service, this.generationOptions!!)

    apiClass.definition.addBodySourcePart(ObjCStringSourcePart(
        """
@protocol $responseSerializerClassName <NSObject>
@optional
/**
 * Deserializes response into specified class
 */
- (id)deserializeResponse:(NSHTTPURLResponse *)response data:(NSData *)rawData toClass:(Class)clz forRequest:(NSURLRequest *)request error:(NSError **)error;
@end
        """
    ))

    apiClass.implementation.addBodySourcePart(ObjCStringSourcePart(
        """
        - (void (^)(NSHTTPURLResponse *, NSData *, NSURLRequest *))deserializationBlockForClass:(Class)clz successBlock:(void (^)(id))successBlock failureBlock:(void (^)(NSError *))failureBlock {
    return ^(NSHTTPURLResponse *response, NSData *rawData, NSURLRequest *request) {
        id result = rawData;
        if (self.responseDeserializer && [self.responseDeserializer respondsToSelector:@selector(deserializeResponse:data:toClass:forRequest:error:)]) {
            NSError * serializationError = nil;
            result = [self.responseDeserializer deserializeResponse:response data:rawData toClass:clz forRequest:request error:&serializationError];
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

  private fun cancelableOperationHeaderForService(service:Service): String {
    val cancellableOperationClassName = this.cancelableOperationClassNameForService(service,this.generationOptions!!)

    return """
      #import <Foundation/Foundation.h>
      /**
       * Protocol used for the ability to cancel some long running operations such as
       * Network requests
       */
      @protocol $cancellableOperationClassName <NSObject>

      /**
       * Cancels operation
       */
      - (void)cancel;
      @end
    """
  }

  private fun httpClientHeaderForService(service: Service): String {
    val cancellableOperationClassName = this.cancelableOperationClassNameForService(service,this.generationOptions!!)
    val httpClientClassName = this.httpClientClassNameForService(service, this.generationOptions!!)
    val requestInterceptorName = this.requestInterceptorClassNameForService(service, this.generationOptions!!)

    val header = """
    #import <Foundation/Foundation.h>

    @protocol $cancellableOperationClassName;
    @protocol $requestInterceptorName <NSObject>
    @optional
    /**
     * Intercepts request os any additional information can be added to it
     */
    - (void)interceptRequest:(NSMutableURLRequest *)request;
    @end


    @interface $httpClientClassName : NSObject
    @property (nonatomic, strong, readonly) NSURLSession *urlSession;
    @property (nonatomic, strong, readonly) NSURL *baseURL;
    @property(nonatomic, copy, readonly) NSString *name;
    @property(nonatomic, copy, readonly) NSString *version;
    @property(nonatomic, strong) NSObject<$requestInterceptorName> * requestInterceptor;

    - (id <$cancellableOperationClassName>)sendRequestWithDescription:(NSString *)description
                                                        path:(NSString *)path
                                                  parameters:(NSDictionary *)parameters
                                                        body:(id)body
                                                     headers:(NSDictionary *)headers
                                                      method:(NSString *)method
                                                successBlock:(void (^)(NSHTTPURLResponse *response, NSData *rawData, NSURLRequest * request))successBlock
                                                failureBlock:(void (^)(NSError *error))failureBlock;

    @end

    """
    return header
  }


  private fun httpClientImplementationForService(service: Service): String {
    val httpClientClassName = this.httpClientClassNameForService(service, this.generationOptions!!)
    val cancellableOperationClassName = this.cancelableOperationClassNameForService(service,this.generationOptions!!)

    val implementation =
        """
    #import "$httpClientClassName.h"
    #import "$cancellableOperationClassName.h"

    @interface $httpClientClassName ()
    @property(nonatomic, strong) NSURLSession *urlSession;
    @property(nonatomic, strong) NSURL *baseURL;
    @property(nonatomic, copy) NSString *name;
    @property(nonatomic, copy) NSString *version;
    @end

    @implementation $httpClientClassName

    - (id)init {
        self = [super init];
        if (self) {
            self.version = @"${service.version}";
            self.name = @"${service.name}";
            self.baseURL = [NSURL URLWithString:@"${service.location}"];
            self.urlSession = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
        }
        return self;
    }

    - (id <$cancellableOperationClassName>)sendRequestWithDescription:(NSString *)description
                                                        path:(NSString *)path
                                                  parameters:(NSDictionary *)parameters
                                                        body:(id)body
                                                     headers:(NSDictionary *)headers
                                                      method:(NSString *)method
                                                successBlock:(void (^)(NSHTTPURLResponse *response, NSData *rawData, NSURLRequest * request))successBlock
                                                failureBlock:(void (^)(NSError *error))failureBlock {


        NSURL *url = self.baseURL;
        NSMutableString *actualPath = [@"" mutableCopy];
        if (path) {
            actualPath = [path mutableCopy];
        }
        if (parameters && parameters.count) {
            NSMutableString *query = [NSMutableString string];
            if ([path rangeOfString:@"?"].location == NSNotFound) {
                [query appendString:@"?"];
            } else {
                [query appendString:@"&"];
            }
            [query appendString:[self URLQueryWithParameters:parameters]];
            [actualPath appendString:query];
        }

        // If we changed actual get part
        if ([actualPath length]) {
            url = [NSURL URLWithString:[NSString stringWithFormat:@"%@%@", self.baseURL, actualPath]];
        }

        NSMutableURLRequest *mutableRequest = [[NSMutableURLRequest alloc] initWithURL:url];
        mutableRequest.HTTPMethod = method;
        if (body) {
            mutableRequest.HTTPBody = body;
        }

        [headers enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            [mutableRequest addValue:obj forHTTPHeaderField:key];
        }];

        if (self.requestInterceptor && [self.requestInterceptor respondsToSelector:@selector(interceptRequest:)]) {
            [self.requestInterceptor interceptRequest:mutableRequest];
        }

        NSURLSessionDataTask *dataTask = [self.urlSession dataTaskWithRequest:mutableRequest completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {

            dispatch_async(dispatch_get_main_queue(), ^{

                // Skip cancelled errors
                if (error && error.code == NSURLErrorCancelled && [error.domain isEqualToString:NSURLErrorDomain]) {
                    return;
                }

                if (error) {
                    failureBlock(error);
                    return;
                }
                successBlock((NSHTTPURLResponse *) response, data, mutableRequest);
            });
        }];
        [dataTask resume];
        return (id <$cancellableOperationClassName>) dataTask;
    }

    - (NSString *)URLQueryWithParameters:(NSDictionary *)parameters {
        NSMutableString *result = [NSMutableString string];
        NSArray *keys = [parameters allKeys];
        for (NSString *key in keys) {
            id value = parameters[key];
            NSString *encodedKey = [self URLEncodedString:[key description]];
            if ([result length]) {
                [result appendString:@"&"];
            }
            [result appendFormat:@"%@=%@", encodedKey, [self URLEncodedString:[value description]]];
        }
        return result;
    }

    - (NSString *)URLEncodedString:(NSString *)string {
        CFStringRef encoded = CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault,
            (__bridge CFStringRef) string,
            NULL,
            CFSTR("!*'\"();:@&=+$,/?%#[]% "),
            kCFStringEncodingUTF8);
        return CFBridgingRelease(encoded);
    }
    @end
   """
    return implementation
  }

}