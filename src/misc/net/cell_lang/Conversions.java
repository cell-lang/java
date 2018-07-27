package net.cell_lang;

import java.util.Arrays;


class Conversions {
  public static Obj convertText(String text) {
    int len = text.length();
    byte[] bytes = new byte[len];
    for (int i=0 ; i < len ; i++) {
      char ch = text.charAt(i);
      if (ch > 255)
        throw new ParsingException(i);
      bytes[i] = (byte) ch;
    }

    return Generated.parse(bytes).getInnerObj();
  }

  public static Value exportAsValue(Obj obj) {
    return obj.getValue();
  }

  public static Obj stringToObj(String str) {
    int[] cps = Miscellanea.codePoints(str);
    return Builder.createTaggedObj(SymbTable.StringSymbId, Builder.createSeq(cps));
  }

  ////////////////////////////////////////////////////////////////////////////

  public static boolean[] toBoolArray(Obj obj) {
    if (obj.isSeq()) {
      boolean[] array = obj.getArray((boolean[]) null);
      return Arrays.copyOf(array, array.length);
    }

    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    boolean[] bools = new boolean[len];
    for (int i=0 ; i < len ; i++)
      bools[i] = elts[i].getBool();
    return bools;
  }

  public static long[] toLongArray(Obj obj) {
    if (obj.isSeq()) {
      long[] array = obj.getArray((long[]) null);
      return Arrays.copyOf(array, array.length);
    }

    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    long[] longs = new long[len];
    for (int i=0 ; i < len ; i++)
      longs[i] = elts[i].getLong();
    return longs;
  }

  public static double[] toDoubleArray(Obj obj) {
    if (obj.isSeq()) {
      double[] array = obj.getArray((double[]) null);
      return Arrays.copyOf(array, array.length);
    }

    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    double[] doubles = new double[len];
    for (int i=0 ; i < len ; i++)
      doubles[i] = elts[i].getLong();
    return doubles;
  }

  public static String[] toSymbolArray(Obj obj) {
    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    String[] symbs = new String[len];
    for (int i=0 ; i < len ; i++)
      symbs[i] = elts[i].toString();
    return symbs;
  }

  public static String[] toStringArray(Obj obj) {
    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    String[] strs = new String[len];
    for (int i=0 ; i < len ; i++)
      strs[i] = elts[i].getString();
    return strs;
  }

  public static Value[] toValueArray(Obj obj) {
    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    Value[] values = new Value[len];
    for (int i=0 ; i < len ; i++)
      values[i] = elts[i].getValue();
    return values;
  }
}
