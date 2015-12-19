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
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod

/**
 * Created by paultaykalo on 12/18/15.
 *
 */
class ObjCHTTPClientGenerator :ObjCProjectStructureGenerator {

  // TODO : Ibject theser values to the generator
  public val nameTransformer = ObjCPropertyNameTransformer()
  public val typeTransformer = ObjCTypeTransformer()

  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {
    projectDSL.services.forEach { service ->
      val mappingsClassName = options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "Client"
      val cancellableOperationClassName = options.prefix + Names.prettifiedName(Names.canonicalName(service.name)) + "CancelableOperation"
      addPregeneratedClientWithName(project, service, mappingsClassName,cancellableOperationClassName)
      val mappingsClass = ObjCClass(mappingsClassName)
      project.classStructure.addClass(mappingsClass)


      addInitMethodForService(mappingsClass, service)
      service.methods.forEach { method ->
        addServiceMethodForService(mappingsClass, method)
      }
    }

  }

  private fun addPregeneratedClientWithName(project:ObjCProject, service: Service, httpClientClassName: String, cancelableOperationClassName: String) {
    val implementation = httpClientImplementation(httpClientClassName, service, cancelableOperationClassName)
    val header = httpClientHeader(cancelableOperationClassName, httpClientClassName)
    project.classStructure.pregeneratedClasses.add(ObjCPregeneratedClass(httpClientClassName, header, implementation))

    val cancellableOperationHeader = cancelableOperationHeader(cancelableOperationClassName)
    project.classStructure.pregeneratedClasses.add(ObjCPregeneratedClass(cancelableOperationClassName,cancellableOperationHeader,null))
  }

  private fun cancelableOperationHeader(cancelableOperationClassName: String): String {
    return """
      #import <Foundation/Foundation.h>
      /**
       * Protocol used for the ability to cancel some long running operations such as
       * Network requests
       */
      @protocol $cancelableOperationClassName <NSObject>

      /**
       * Cancels operation
       */
      - (void)cancel;
      @end
    """
  }


  private fun addInitMethodForService(mappingsClass: ObjCClass, service: Service) {
    // Version and location setup
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("version", ObjCType("NSString", isReference = true)))
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("name", ObjCType("NSString", isReference = true)))
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("baseURL", ObjCType("NSURL", isReference = true)))
    mappingsClass.definition.addPropertyDefinition(ObjCPropertyDefinition("urlSession", ObjCType("NSURLSession", isReference = true)))

    val initMethod = ObjCMethod("init", ObjCMethod.ObjCMethodType.INSTANCE, "id")
    val initMethodImplementationSourcePart = ObjCMethodImplementationSourcePart(initMethod)
    initMethodImplementationSourcePart.addSourcePart(ObjCStringSourcePart(
    ));
    mappingsClass.implementation.addBodySourcePart(initMethodImplementationSourcePart)
  }

  private fun addServiceMethodForService(mappingsClass: ObjCClass, method: ServiceMethod) {
    val methodName = Names.prettifiedName(Names.canonicalName(method.name)).decapitalize()
    val serviceMethod = ObjCMethod(methodName, ObjCMethod.ObjCMethodType.INSTANCE, "void")

    // Injecting Path Parameters to method
    if (method.pathParameters != null) {
      method.pathParameters.forEach { paramName ->
        val parameterName = Names.prettifiedName(Names.canonicalName(paramName))
        val checkedParameterName = nameTransformer.propertyNameFrom(parameterName)
        serviceMethod.addParameter("NSString *", checkedParameterName)
      }
    }
    // Injecting Method Parameters to method
    if (method.parameters != null) {
      method.parameters.fields.forEach { field ->
        val parameterName = Names.prettifiedName(Names.canonicalName(field.name))
        val checkedParameterName = nameTransformer.propertyNameFrom(parameterName)
        serviceMethod.addParameter(typeTransformer.objCType(field.type).toString(), checkedParameterName)
      }
    }
    mappingsClass.implementation.addMethod(serviceMethod)
    mappingsClass.definition.addMethod(serviceMethod)
  }


  private fun httpClientHeader(cancelableOperationClassName: String, httpClientClassName: String): String {
    val header = """
    #import <Foundation/Foundation.h>

    @protocol $cancelableOperationClassName;

    @interface $httpClientClassName : NSObject
    @property (nonatomic, strong, readonly) NSURLSession *urlSession;
    @property (nonatomic, strong, readonly) NSURL *baseURL;

    - (id <$cancelableOperationClassName>)sendRequestWithDescription:(NSString *)description
                                                        path:(NSString *)path
                                                  parameters:(NSDictionary *)parameters
                                                        body:(id)body
                                                     headers:(NSDictionary *)headers
                                                      method:(NSString *)method
                                                successBlock:(void (^)(NSHTTPURLResponse *response, NSData *rawData))successBlock
                                                failureBlock:(void (^)(NSError *error))failureBlock;

    @end

    """
    return header
  }

  private fun httpClientImplementation(httpClientClassName: String, service: Service, cancelableOperationClassName: String): String {
    val implementation =
        """
    #import "$httpClientClassName.h"
    #import "$cancelableOperationClassName.h"

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

    - (id <$cancelableOperationClassName>)sendRequestWithDescription:(NSString *)description
                                                        path:(NSString *)path
                                                  parameters:(NSDictionary *)parameters
                                                        body:(id)body
                                                     headers:(NSDictionary *)headers
                                                      method:(NSString *)method
                                                successBlock:(void (^)(NSHTTPURLResponse *response, NSData *rawData))successBlock
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
                successBlock((NSHTTPURLResponse *) response, data);
            });
        }];
        [dataTask resume];
        return (id <$cancelableOperationClassName>) dataTask;
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