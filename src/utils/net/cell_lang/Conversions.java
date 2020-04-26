package net.cell_lang;

import java.util.Arrays;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


class Conversions {
  public static Obj convertText(String text) {
    TokenStream tokens = new Tokenizer(new CharStream(new StringReader(text)));
    Parser parser = new Generated.Parser(tokens);
    Obj obj = parser.parseObj();
    parser.checkEof();
    return obj;
  }

  public static String exportAsText(Obj obj) {
    return obj.toString();
  }

  public static Obj stringToObj(String str) {
    //## THIS ONE IS REAL BAD TOO. IT SHOULD USE THE MINIMUM SIZE ARRAY POSSIBLE!
    int[] cps = Miscellanea.codePoints(str);
    return Builder.createTaggedObj(SymbObj.StringSymbId, Builder.createSeq(cps));
  }

  ////////////////////////////////////////////////////////////////////////////

  public static Obj dateToObj(LocalDate date) {
    return Builder.createTaggedIntObj(SymbObj.DateSymbId, date.toEpochDay());
  }

  public static LocalDate objToDate(Obj date) {
    return LocalDate.ofEpochDay(date.getInnerLong());
  }

  public static Obj dateTimeToObj(LocalDateTime time) {
    long epochSecond = time.toEpochSecond(ZoneOffset.UTC);
    int nanosecs = time.getNano();
    if (epochSecond >= -9223372036L) {
      if (epochSecond < 9223372036L | (epochSecond == 9223372036L & nanosecs <= 854775807)) {
        long epochNanoSecs = 1000000000 * epochSecond + nanosecs;
        return Builder.createTaggedIntObj(SymbObj.TimeSymbId, epochNanoSecs);
      }
    }
    else if (epochSecond == -9223372037L & nanosecs >= 145224192) {
      long epochNanoSecs = -9223372036000000000L - (1000000000 - nanosecs);
      return Builder.createTaggedIntObj(SymbObj.TimeSymbId, epochNanoSecs);
    }
    throw new RuntimeException("DateTime is outside the supported range: " + time.toString());
  }

  public static LocalDateTime objToDateTime(Obj time) {
    long epochNanoSecs = time.getInnerLong();
    long epochSecond = epochNanoSecs >= 0 ?
      epochNanoSecs / 1000000000 :
      ((epochNanoSecs + 999999999) / 1000000000) - 1;
    int nanosecs = (int) (epochNanoSecs - 1000000000 * epochSecond);
    return LocalDateTime.ofEpochSecond(epochSecond, nanosecs, ZoneOffset.UTC);
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
      doubles[i] = elts[i].getDouble();
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

  public static String[] toTextArray(Obj obj) {
    Obj[] elts = obj.getArray((Obj[]) null);
    int len = elts.length;
    String[] strs = new String[len];
    for (int i=0 ; i < len ; i++)
      strs[i] = exportAsText(elts[i]);
    return strs;
  }
}
