package com.poolik.classfinder.otherTestClasses;

import com.poolik.classfinder.ClassFinderException;

@Deprecated
public class ConcreteClass extends AbstractClass {

  @Deprecated
  private String test = "test";
  public int anotherValue = 0;

  private void test() throws ClassFinderException {}
  public static String another() throws ClassFinderException {
    return "";
  }
}
