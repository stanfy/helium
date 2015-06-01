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
class JUnitReportGenerator implements HeliumTestLog, CheckListener {

  private final Map<BehaviourSuite, StringBuilder> outputs = [:]
  private ArrayList<StringBuilder> currentOut = new ArrayList<>(3)
  private int depth

  @Override
  void onSuiteStarted(final BehaviourSuite suite) {
    if (depth < 2) {
      currentOut << new StringBuilder()
      outputs[suite] = currentOut.last()
    }
    depth++
  }

  @Override
  void onCheckStarted(final BehaviourCheck check) {

  }

  @Override
  void onCheckDone(final BehaviourCheck check) {

  }

  @Override
  void onSuiteDone(final BehaviourSuite suite) {
    depth--
    if (depth < 2) {
      currentOut.pop()
    }
  }

  @Override
  void write(String fmt, Object... args) {
    if (currentOut.empty) {
      throw new IllegalStateException("No current output (d=$depth, oc=${outputs.size()})")
    }
    if (args.length > 0) {
      currentOut.last() << String.format(fmt, args)
    } else {
      currentOut.last() << fmt
    }
    currentOut.last() << "\n"
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
          def out = outputs.get(check)
          if (out) {
            "system-out"(out.toString())
          }
        }
      }

      def out = outputs.get(suite)
      if (out) {
        "system-out"(out.toString())
      }
    }
  }

}
