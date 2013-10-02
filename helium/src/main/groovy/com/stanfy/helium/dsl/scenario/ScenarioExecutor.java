package com.stanfy.helium.dsl.scenario;

import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;

/**
 * Executor for some scenario.
 */
public interface ScenarioExecutor {

  Object performMethod(Service service, ServiceMethod method, ServiceMethodRequestValues request);

}
