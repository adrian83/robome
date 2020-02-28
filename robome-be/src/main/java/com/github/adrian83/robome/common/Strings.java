package com.github.adrian83.robome.common;

public final class Strings {

  private Strings() {}

  public static String fromEnd(String str, int chars) {
    var len = str.length();
    return str.substring(len - (chars + 1), len - 1);
  }

  public static String fromBegining(String str, int chars) {
    return str.substring(0, chars);
  }
}
