package com.stanfy.helium.internal.model.tests;

import com.stanfy.helium.internal.MethodsExecutor;
import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.*;

public final class CheckGroup {

  private final List<? extends CheckableItem> children;
  private final MethodsExecutor executor;

  public CheckGroup(final List<? extends CheckableItem> children, final MethodsExecutor executor) {
    this.children = children;
    this.executor = executor;
  }

  public BehaviourSuite run(final String name) {
    if (children.isEmpty()) {
      return BehaviourSuite.EMPTY;
    }
    Duration resultDuration = Duration.ZERO;
    BehaviourCheck.Result resultType = PASSED;
    ArrayList<BehaviourCheck> childResults = new ArrayList<BehaviourCheck>(children.size());
    for (CheckableItem c: children) {
      BehaviourCheck check = c.check(executor);
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
    BehaviourSuite res = new BehaviourSuite();
    res.setName(name);
    res.setChildren(childResults);
    res.setResult(resultType);
    res.setTime(resultDuration);
    return res;
  }

}
