package com.stanfy.helium.internal;

import com.stanfy.helium.dsl.MethodExecutionResult;
import com.stanfy.helium.internal.ServiceMethodRequestValues;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;

/**
 * Requests executor.
 */
public interface MethodsExecutor {

  MethodExecutionResult performMethod(Service service, ServiceMethod method, ServiceMethodRequestValues request);

}
