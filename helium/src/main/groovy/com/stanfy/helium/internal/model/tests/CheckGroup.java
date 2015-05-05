package com.stanfy.helium.internal.model.tests;

import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED;
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PASSED;
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PENDING;

public final class CheckGroup {

  private final List<BehaviourDescription> children;

  public CheckGroup(final List<BehaviourDescription> children) {
    this.children = children;
  }

  public BehaviourSuite run(final String name) {
    if (children.isEmpty()) {
      return BehaviourSuite.EMPTY;
    }
    Duration resultDuration = Duration.ZERO;
    BehaviourCheck.Result resultType = PASSED;
    ArrayList<BehaviourCheck> childResults = new ArrayList<BehaviourCheck>(children.size());
    for (BehaviourDescription d: children) {
      BehaviourSuite check = d.check();
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
