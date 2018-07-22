package net.cell_lang;


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

    return Generated.parse(bytes);
  }

  public static Value exportAsValue(Obj obj) {
    return obj.getValue();
  }

  public static Obj stringToObj(String str) {
    int[] cps = Miscellanea.codePoints(str);
    Int32ArrayObj obj = IntArrayObjs.create(cps);
    return Builder.createTaggedObj(SymbTable.StringSymbId, obj);
  }

  ////////////////////////////////////////////////////////////////////////////

  public static boolean[] toBoolArray(Obj obj) {
    return obj.getArray((boolean[]) null);
  }

  public static long[] toLongArray(Obj obj) {
    return obj.getArray((long[]) null);
  }

  public static double[] toDoubleArray(Obj obj) {
    return obj.getArray((double[]) null);
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
