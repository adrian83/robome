package com.github.adrian83.robome.common;

import static java.util.Optional.ofNullable;

public final class Strings {

  private static final int MIN_TEXT_LENGTH = 3;

  private Strings() {}

  public static String fromEnd(String str, int chars) {
    var len = str.length();
    return str.substring(len - (chars + 1), len - 1);
  }

  public static String fromBegining(String str, int chars) {
    return str.substring(0, chars);
  }

  public static String hideText(String text) {
    return ofNullable(text)
        .filter(p -> p.length() > MIN_TEXT_LENGTH)
        .map(p -> new String(new char[p.length() - 2]).replace("\0", "*"))
        .map(s -> fromBegining(text, 1) + s + fromEnd(text, 1))
        .orElse("**");
  }
}
