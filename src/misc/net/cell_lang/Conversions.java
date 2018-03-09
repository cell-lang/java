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

    Token[] tokens = Lexer.lex(bytes);
    return Parser.parse(tokens);
  }

  public static Value exportAsValue(Obj obj) {
    return obj.getValue();
  }

  public static Obj stringToObj(String str) {
    int[] cps = Miscellanea.codePoints(str);
    int len = cps.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = IntObj.get(cps[i]);
    return new TaggedObj(SymbTable.StringSymbId, new MasterSeqObj(objs));
  }

  ////////////////////////////////////////////////////////////////////////////

  public static boolean[] ToBoolArray(Obj obj) {
    int size = obj.getSize();
    boolean[] array = new boolean[size];
    SeqOrSetIter it = obj.getSeqOrSetIter();
    int idx = 0;
    while (!it.done()) {
      array[idx++] = it.get().getBool();
      it.next();
    }
    return array;
  }

  public static long[] toLongArray(Obj obj) {
    int size = obj.getSize();
    long[] array = new long[size];
    SeqOrSetIter it = obj.getSeqOrSetIter();
    int idx = 0;
    while (!it.done()) {
      array[idx++] = it.get().getLong();
      it.next();
    }
    return array;
  }

  public static double[] toDoubleArray(Obj obj) {
    int size = obj.getSize();
    double[] array = new double[size];
    SeqOrSetIter it = obj.getSeqOrSetIter();
    int idx = 0;
    while (!it.done()) {
      array[idx++] = it.get().getDouble();
      it.next();
    }
    return array;
  }

  public static String[] toSymbolArray(Obj obj) {
    int size = obj.getSize();
    String[] array = new String[size];
    SeqOrSetIter it = obj.getSeqOrSetIter();
    int idx = 0;
    while (!it.done()) {
      array[idx++] = it.get().toString();
      it.next();
    }
    return array;
  }

  public static String[] toStringArray(Obj obj) {
    int size = obj.getSize();
    String[] array = new String[size];
    SeqOrSetIter it = obj.getSeqOrSetIter();
    int idx = 0;
    while (!it.done()) {
      array[idx++] = it.get().getString();
      it.next();
    }
    return array;
  }

  public static Value[] toValueArray(Obj obj) {
    int size = obj.getSize();
    Value[] array = new Value[size];
    SeqOrSetIter it = obj.getSeqOrSetIter();
    int idx = 0;
    while (!it.done()) {
      array[idx++] = it.get().getValue();
      it.next();
    }
    return array;
  }
}
