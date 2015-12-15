package com.stanfy.helium.handler.tests

import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.model.tests.CheckListener
import groovy.transform.PackageScope
import groovy.xml.MarkupBuilder
import okio.BufferedSink

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PENDING

@PackageScope
class JUnitReportGenerator extends HeliumTestLogMemory implements CheckListener {

  private ArrayList<BehaviourCheck> currentSuite = new ArrayList<>(3)
  private int depth

  @Override
  protected BehaviourCheck currentCheck() {
    return currentSuite.empty ? null : currentSuite.last()
  }

  @Override
  void onSuiteStarted(final BehaviourSuite suite) {
    if (depth < 2) {
      currentSuite << suite
    }
    depth++
  }

  @Override
  void onCheckStarted(final BehaviourCheck check) { }
  @Override
  void onCheckDone(final BehaviourCheck check) { }

  @Override
  void onSuiteDone(final BehaviourSuite suite) {
    depth--
    if (depth < 2) {
      currentSuite.pop()
    }
  }

  private void collectDescription(final StringBuilder out, final BehaviourCheck check) {
    out << check.name << "\n"
    if (check.description) {
      out << check.description << "\n"
    }
    out << "\n"
    if (check instanceof BehaviourSuite) {
      check.children.each {
        if (it.result == FAILED) {
          collectDescription(out, it)
        }
      }
    }
  }

  public void generateInto(final BufferedSink output, final BehaviourSuite suite) throws IOException {
    MarkupBuilder xml = new MarkupBuilder(new OutputStreamWriter(output.outputStream(), "UTF-8"))
    xml.testsuite(tests: suite.children.size()) {
      suite.children.each { BehaviourCheck check ->
        testcase(classname: suite.canonicalName, name: check.name, time: check.time.millis) {
          if (check.result == PENDING) {
            skipped()
          } else if (check.result == FAILED) {
            StringBuilder desc = new StringBuilder()
            collectDescription(desc, check)
            failure(desc.toString())
          }
          def out = log(check)
          if (out) {
            "system-out"(out.toString())
          }
        }
      }

      def out = log(suite)
      if (out) {
        "system-out"(out.toString())
      }
    }
  }

}
