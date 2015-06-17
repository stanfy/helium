package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.internal.MethodsExecutor
import com.stanfy.helium.internal.dsl.BehaviourDescriptionBuilder
import com.stanfy.helium.internal.dsl.ExecutorDsl
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.model.tests.CheckListener
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.joda.time.Duration

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PASSED

class CheckBuilder implements BehaviorDescriptionContainer {

  private final ArrayList<CheckRunner> checks = new ArrayList<>()
  private final ArrayList<Closure<Void>> before = new ArrayList<>()
  private final ArrayList<Closure<Void>> after = new ArrayList<>()

  private final ProjectDsl project
  private final Service service
  private final MethodsExecutor executor
  private final CheckListener listener

  public CheckBuilder(final ProjectDsl project, final Service service, final MethodsExecutor executor, final CheckListener listener) {
    if (project == null) {
      throw new IllegalArgumentException("Null project")
    }
    this.project = project
    this.service = service
    this.executor = executor
    this.listener = listener
  }

  /**
   * Example:
   * it("x should be equal to 2", { x == 2 })
   * it("message should not be null", { assert msg != nil })
   */
  public void it(String name, Closure<?> action) {
    BehaviourCheck res = new BehaviourCheck(name: name)
    def runner = {
      long startTime = System.currentTimeMillis()
      try {
        def checkResult = runAction(action)
        if (checkResult instanceof Boolean) {
          res.result = (checkResult as Boolean) ? PASSED : FAILED
        } else {
          res.result = PASSED
        }
      } catch (Throwable e) {
        res.result = FAILED
        res.description = e.message
      } finally {
        res.time = Duration.millis(System.currentTimeMillis() - startTime)
      }
      return res
    }
    checks.add(new CheckRunner(check: res, run: runner))
  }

  /**
   * For nested spec.
   */
  public BehaviourDescriptionBuilder describe(String name) {
    return new BehaviourDescriptionBuilder(name, this, project)
  }

  @Override
  void addBehaviourDescription(final BehaviourDescription desc) {
    desc.service = service
    checks.add new CheckRunner(run: { desc.check(executor, listener) })
  }

  public void beforeEach(Closure<Void> action) {
    before.add action
  }

  public void afterEach(Closure<Void> action) {
    after.add action
  }

  /**
   * Example:
   * xit("x should be not zero", { not implemented yet })
   */
  public void xit(String name, Closure<?> ignored) {
    xit(name)
  }

  private void ignored(String name, boolean suite) {
    def runner = { suite ? BehaviourSuite(name: name) : new BehaviourCheck(name: name) }
    checks.add new CheckRunner(run: runner, skipped: true)
  }

  /**
   * Example:
   * xit("x should be not zero")
   */
  public void xit(String name) {
    ignored(name, false)
  }

  public void it(String name) {
    xit(name)
  }

  public def xdescribe(String name) {
    ignored(name, true)
    return [spec : { /* Nothing. */ }]
  }

  @CompileStatic
  @PackageScope List<CheckableItem> makeChecks() {
    return checks.collect { runner ->
      return { executor, CheckListener listener ->
        try {
          if (!runner.skipped) {
            if (runner.check) { listener.onCheckStarted(runner.check) }
            before.each { runAction(it) }
          }
          BehaviourCheck res = (BehaviourCheck) runAction(runner.run)
          if (runner.skipped) {
            notifySkippedListener(res)
          }
          return res
        } finally {
          if (!runner.skipped) {
            after.each { runAction(it) }
            if (runner.check) { listener.onCheckDone(runner.check) }
          }
        }
      } as CheckableItem
    }
  }

  private void notifySkippedListener(BehaviourCheck res) {
    if (res instanceof BehaviourSuite) {
      listener.onSuiteStarted(res)
      listener.onSuiteDone(res)
    } else {
      listener.onCheckStarted(res)
      listener.onCheckDone(res)
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

  private static def runAction(Closure<?> action) {
    return action()
  }

  public ExecutorDsl getService() {
    if (service != null) {
      return serviceDsl(service)
    }
    throw new IllegalStateException("There is no bound service")
  }

  public ExecutorDsl service(final String name) {
    Service s = project.serviceByName(name)
    if (s != null) {
      return serviceDsl(s)
    }
    throw new IllegalArgumentException("Service $name does not exist")
  }

  private ExecutorDsl serviceDsl(final Service service) {
    return new ExecutorDsl(service, executor)
  }

  private static class CheckRunner {
    BehaviourCheck check
    Closure<BehaviourCheck> run
    boolean skipped
  }

}
