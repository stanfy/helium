package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.dsl.scenario.ScenarioExecutor
import com.stanfy.helium.internal.dsl.ExecutorDsl
import com.stanfy.helium.model.Checkable
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.utils.DslUtils
import groovy.transform.PackageScope
import org.joda.time.Duration

import static com.stanfy.helium.internal.model.tests.Util.errorStack
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PASSED

class CheckBuilder { //extends ExecutorDsl {

  private final ArrayList<CheckRunner> checks = new ArrayList<>()
  private final ArrayList<Closure<Void>> before = new ArrayList<>()
  private final ArrayList<Closure<Void>> after = new ArrayList<>()

//  CheckBuilder(Service service, ScenarioExecutor executor) {
//    super(service, executor)
//  }

  /**
   * Example:
   * it("x should be equal to 2", { x == 2 })
   * it("message should not be null", { assert msg != nil })
   */
  public void it(String name, Closure<?> action) {
    def runner = {
      BehaviourCheck res = new BehaviourCheck(name: name, result: FAILED)
      long startTime = System.currentTimeMillis()
      try {
        def checkResult = runWithExecutor(action)
        if (checkResult instanceof Boolean) {
          res.result = (checkResult as Boolean) ? PASSED : FAILED
        } else {
          res.result = PASSED
        }
      } catch (Exception e) {
        res.description = errorStack(e) // TODO Consider adding message only.
      } finally {
        res.time = Duration.millis(System.currentTimeMillis() - startTime)
      }
      return res
    }
    checks.add(runner as CheckRunner)
  }

  /**
   * For nested spec.
   */
  public void describe(String name, Closure<Void> spec) {
    BehaviourDescription desc = new BehaviourDescription(name: name, action: spec)
    checks.add({
      return desc.check()
    } as CheckRunner)
  }

  public void before(Closure<Void> action) {
    before.add action
  }

  public void after(Closure<Void> action) {
    after.add action
  }

  /**
   * Example:
   * xit("x should be not zero", { not implemented yet })
   */
  public void xit(String name, Closure<?> ignored) {
    xit(name)
  }

  /**
   * Example:
   * xit("x should be not zero")
   */
  public void xit(String name) {
    def runner = {
      return new BehaviourCheck(name: name)
    }
    checks.add(runner)
  }

  public void it(String name) {
    xit(name)
  }

  public void xdescribe(String name) {
    xit(name)
  }

  public void xdescribe(String name, Closure<Void> ignored) {
    xdescribe(name)
  }

  public void describe(String name) {
    xdescribe(name)
  }

  @PackageScope List<Checkable> makeChecks() {
    return checks.collect { runner ->
      return {
        try {
          before.each { runWithExecutor(it) }
          return runner.run()
        } finally {
          after.each { runWithExecutor(it) }
        }
      } as Checkable
    }
  }

  @PackageScope def itArgument() {
    return { Object[] args ->
      if (args.length == 2) {
        it(args[0] as String, args[1] as Closure<?>)
      } else {
        it(args[0] as String)
      }
    }
  }

  private def runWithExecutor(Closure<?> action) {
//    ExecutorDsl executor = new ExecutorDsl(this.service, this.executor)
//    return DslUtils.runWithProxy(executor, action)
    return action()
  }

  private interface CheckRunner {
    BehaviourCheck run()
  }

}
