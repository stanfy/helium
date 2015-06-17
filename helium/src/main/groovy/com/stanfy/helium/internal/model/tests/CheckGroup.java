package com.stanfy.helium.internal.model.tests;

import com.stanfy.helium.internal.MethodsExecutor;
import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.*;

public final class CheckGroup {

  private final List<? extends CheckableItem> children;
  private final MethodsExecutor executor;
  private final CheckListener listener;

  public CheckGroup(final List<? extends CheckableItem> children,
                    final MethodsExecutor executor, final CheckListener listener) {
    this.children = children;
    this.executor = executor;
    this.listener = listener;
  }

  public BehaviourSuite run(final String name) {
    BehaviourSuite res = new BehaviourSuite();
    res.setName(name);
    Duration resultDuration = Duration.ZERO;
    ArrayList<BehaviourCheck> childResults = new ArrayList<BehaviourCheck>(children.size());
    listener.onSuiteStarted(res);

    try {
      BehaviourCheck.Result resultType = children.isEmpty() ? PENDING : PASSED;
      for (CheckableItem c: children) {
        BehaviourCheck check = c.check(executor, listener);
        childResults.add(check);
        resultDuration = resultDuration.plus(check.getTime());
        switch (check.getResult()) {
          case PENDING:
            if (resultType == PASSED) {
              resultType = PENDING;
            }
            break;
          case FAILED:
            resultType = FAILED;
            break;
          default:
            // Nothing.
        }
      }
      res.setResult(resultType);
    } catch (Throwable e) {
      res.setResult(FAILED);
      if (e instanceof AssertionError) {
        res.setDescription(e.getMessage());
      } else {
        res.setDescription(Util.errorStack(e));
      }
    } finally {
      res.setChildren(childResults);
      res.setTime(resultDuration);
      listener.onSuiteDone(res);
    }

    return res;
  }

}
