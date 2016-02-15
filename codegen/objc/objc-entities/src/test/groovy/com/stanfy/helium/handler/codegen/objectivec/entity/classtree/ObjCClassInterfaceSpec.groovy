package com.stanfy.helium.handler.codegen.objectivec.entity.classtree

import spock.lang.Specification

import java.util.regex.Pattern
/**
 * Created by paultaykalo on 1/29/16.
 * Tests for objc class interface
 */

class ObjCClassInterfaceSpec extends Specification {

  String className
  def generator = { String alphabet, int n ->
    new Random().with {
      (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
    }
  }
  def pattern(String s) {
    return Pattern.compile(".*" + s +".*", Pattern.DOTALL)
  }

  def setup() {
    className = "Class" + generator( (('A'..'Z')+('0'..'9')+('a'..'z')).join(""), 15 )
  }

  def "should use specified classname"() {
    when:
    def inteface = new ObjCClassInterface(className)

    then:
    inteface != null
  }

  def "should include @interface and @end parts" () {
    when:
    def inteface = new ObjCClassInterface(className)

    then:
    inteface.asString().matches(pattern("@interface\\s+${className}.+@end"))
  }

  def "should place import classes before interface" () {
    when:
    def inteface = new ObjCClassInterface(className)
    inteface.importClassWithName("Cool")

    then:
    inteface.asString().matches(pattern("#import\\s+\"Cool.h\".*\\n.*@interface\\s+${className}.+@end"))
  }

  def "should place import frameworks before interface" () {
    when:
    def inteface = new ObjCClassInterface(className)
    inteface.importFrameworkWithName("Mantle/Mantle")

    then:
    inteface.asString().matches(pattern("#import\\s+<Mantle/Mantle.h>.*\n.*@interface\\s+${className}.+@end"))
  }

  def "should be inherited from superclass" () {
    when:
    def inteface = new ObjCClassInterface(className)
    inteface.superClassName = "AwesomeClassName"

    then:
    inteface.asString().matches(pattern(".*@interface\\s+${className}.*:.*AwesomeClassName.*@end"))
  }

  def "should implement protocols" () {
    when:
    def inteface = new ObjCClassInterface(className)
    inteface.getImplementedProtocols().add("AwesomeProtocol")

    then:
    inteface.asString().matches(pattern(".*@interface\\s+${className}.*<.*AwesomeProtocol.*>.*@end"))
  }

}
